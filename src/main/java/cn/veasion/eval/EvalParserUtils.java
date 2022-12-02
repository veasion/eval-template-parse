package cn.veasion.eval;

import java.io.StringReader;
import java.util.Map;

/**
 * 表达式解析工具类
 *
 * @author luozhuowei
 * @date 2022/12/2
 */
public class EvalParserUtils {

    /**
     * 表达式解析
     *
     * @param object 对象
     * @param eval   表达式，支持对象属性、数组下标、变量、条件符、三目运算
     * @return 结果
     */
    public static Object eval(Object object, String eval) throws Exception {
        return eval(object, eval, null);
    }

    /**
     * 表达式解析
     *
     * @param object 对象
     * @param eval   表达式，支持对象属性、数组下标、变量、条件符、三目运算
     * @param varMap 变量结果
     * @return 结果
     */
    public static Object eval(Object object, String eval, Map<String, Object> varMap) throws Exception {
        EvalParser evalParser = new EvalParser(new StringReader(eval));
        evalParser.setObject(object);
        evalParser.setVarTrace(varMap != null);
        Object result = evalParser.eval();
        if (varMap != null) {
            Map<String, Object> _varMap = evalParser.getVarMap();
            if (_varMap != null) {
                varMap.putAll(_varMap);
            }
        }
        return result;
    }

    /**
     * 模板表达式解析
     *
     * @param object   对象
     * @param template 模板，支持通过表达式，如 ${name}
     * @return 结果
     */
    public static Object template(Object object, String template) throws Exception {
        return template(object, template, null);
    }

    /**
     * 模板表达式解析
     *
     * @param object   对象
     * @param template 模板，支持通过表达式，如 ${name}
     * @param varMap   变量结果
     * @return 结果
     */
    public static Object template(Object object, String template, Map<String, Object> varMap) throws Exception {
        return template(object, template, "${", "}", varMap);
    }

    /**
     * 模板表达式解析
     *
     * @param object   对象
     * @param template 模板，支持通过 prefix和suffix包裹表达式，如 ${name}
     * @param prefix   表达式包裹前缀，如 ${
     * @param suffix   表达式包裹后缀，如 }
     * @param varMap   变量结果
     * @return 结果
     */
    public static Object template(Object object, String template, String prefix, String suffix, Map<String, Object> varMap) throws Exception {
        EvalParser evalParser = new EvalParser(new StringReader(""));
        evalParser.setVarTrace(varMap != null);
        Object result = evalParser.evalReplace(object, template, prefix, suffix);
        if (varMap != null) {
            Map<String, Object> _varMap = evalParser.getVarMap();
            if (_varMap != null) {
                varMap.putAll(_varMap);
            }
        }
        return result;
    }

}
