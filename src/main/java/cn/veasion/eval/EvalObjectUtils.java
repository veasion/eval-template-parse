package cn.veasion.eval;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * EvalObjectUtils
 *
 * @author luozhuowei
 * @date 2021/11/24
 */
public class EvalObjectUtils {

    private static final Map<Class<?>, Map<String, Method>> METHOD_CACHE_MAP = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE_MAP = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, Method>> GET_METHOD_CACHE_MAP = new ConcurrentHashMap<>();

    public static BigDecimal toBigDecimal(Object object) {
        if (object == null || object instanceof BigDecimal) {
            return (BigDecimal) object;
        }
        return new BigDecimal(object.toString());
    }

    public static boolean equals(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }
        return Objects.equals(obj1, obj2) || String.valueOf(obj1).equals(String.valueOf(obj2));
    }

    public static int compareTo(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return 0;
        }
        if (obj1 instanceof Number && obj2 instanceof Number) {
            return Double.compare(((Number) obj1).doubleValue(), ((Number) obj2).doubleValue());
        } else {
            BigDecimal o1 = obj1 != null ? new BigDecimal(obj1.toString()) : null;
            BigDecimal o2 = obj2 != null ? new BigDecimal(obj2.toString()) : null;
            if (o1 == null) {
                return -1;
            } else if (o2 == null) {
                return 1;
            }
            return o1.compareTo(o2);
        }
    }

    public static boolean isTrue(Object object) {
        return !(object == null || Boolean.FALSE.equals(object) || "false".equalsIgnoreCase(object.toString()) || "".equals(object.toString().trim()) || "0".equals(object.toString()));
    }

    public static String evalReplace(Object object, String str, String prefix, String suffix, BiFunction<Object, String, Object> fun) {
        StringBuilder sb = new StringBuilder();
        int startIndex = 0, index, endIndex;
        while ((index = str.indexOf(prefix, startIndex)) > -1) {
            sb.append(str, startIndex, index);
            endIndex = str.indexOf(suffix, index + prefix.length());
            if (endIndex == -1) {
                startIndex = index;
                break;
            }
            String eval = str.substring(index + prefix.length(), endIndex);
            if (!"".equals(eval.trim())) {
                Object result = fun.apply(object, eval);
                sb.append(result != null ? result.toString() : "");
            }
            startIndex = endIndex + suffix.length();
        }
        sb.append(str.substring(startIndex));
        return sb.toString();
    }

    public static Object parse(Object object, Object param) {
        if (isArray(object)) {
            return parseArray(object, param);
        } else {
            return parseObject(object, String.valueOf(param));
        }
    }

    public static Object parseArray(Object object, Object indexObject) {
        int index = indexObject instanceof Number ? ((Number) indexObject).intValue() : Integer.parseInt(indexObject.toString());
        if (object != null) {
            try {
                if (object instanceof Collection) {
                    return ((Collection<?>) object).toArray()[index];
                } else if (object instanceof Object[]) {
                    Object[] array = (Object[]) object;
                    return array[index];
                } else if (object.getClass().isArray()) {
                    return Array.get(object, index);
                }
                throw new RuntimeException(String.format("%s (%s) 不是一个 array 类型", object, object.getClass().getName()));
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new RuntimeException(String.format("%s 数组越界 => %s", object, index));
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Object parseObject(Object object, String key) {
        if (object instanceof Map) {
            return ((Map<?, ?>) object).get(key);
        } else if (object instanceof Function) {
            return ((Function<String, ?>) object).apply(key);
        } else {
            try {
                return reflect(object, key);
            } catch (Exception e) {
                return null;
            }
        }
    }

    private static Object reflect(Object object, String key) throws InvocationTargetException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        Method methodCache = getCache(METHOD_CACHE_MAP, clazz, key);
        if (methodCache != null) {
            return methodCache.invoke(object);
        }
        Field fieldCache = getCache(FIELD_CACHE_MAP, clazz, key);
        if (fieldCache != null) {
            return fieldCache.get(object);
        }
        Method getMethodCache = getCache(GET_METHOD_CACHE_MAP, clazz, key);
        if (getMethodCache != null) {
            return getMethodCache.invoke(object, key);
        }
        // method
        Method getMethod = null;
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            if (method.getParameterCount() == 0 && methodName.equalsIgnoreCase("get" + key)) {
                putCache(METHOD_CACHE_MAP, clazz, key, method);
                return method.invoke(object);
            }
            if ("get".equals(methodName) && method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom(key.getClass())) {
                getMethod = method;
            }
        }
        // field
        Field field = getByField(clazz, key);
        if (field != null) {
            field.setAccessible(true);
            if (field.getName().equals(key)) {
                putCache(FIELD_CACHE_MAP, clazz, key, field);
                return field.get(object);
            }
        }
        // get method
        if (getMethod != null) {
            putCache(GET_METHOD_CACHE_MAP, clazz, key, getMethod);
            return getMethod.invoke(object, key);
        }
        return null;
    }

    private static Field getByField(Class<?> clazz, String key) {
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals(key)) {
                    return field;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private static boolean isNumber(Object object) {
        return object != null && object.toString().matches("\\d+");
    }

    private static boolean isArray(Object object) {
        return object != null && (object instanceof Collection || object.getClass().isArray());
    }

    private static <T> T getCache(Map<Class<?>, Map<String, T>> cacheMap, Class<?> clazz, String key) {
        Map<String, T> clazzMap = cacheMap.get(clazz);
        return clazzMap != null ? clazzMap.get(key) : null;
    }

    private static <T> void putCache(Map<Class<?>, Map<String, T>> cacheMap, Class<?> clazz, String key, T value) {
        cacheMap.compute(clazz, (k, v) -> {
            if (v == null) {
                v = new ConcurrentHashMap<>();
            }
            v.put(key, value);
            return v;
        });
    }
}
