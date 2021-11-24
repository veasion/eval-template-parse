package cn.veasion.eval;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
