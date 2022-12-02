package cn.veasion.eval;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 计算器工具类
 *
 * @author luozhuowei
 * @date 2022/12/2
 */
public class CalculatorUtils {

    /**
     * 计算常规数学表达式
     *
     * @param eval 数学表达式<br>
     *             示例一：1+2+3-5*2+8/4+3^2+√(4+6-2) <br>
     *             示例二：2+5*(2-6*(3+1))/3 <br>
     *             示例三：4.99+(5.99+6.99)*1.06^2+√4
     * @return 计算结果
     */
    public static BigDecimal calc(String eval) throws Exception {
        return calc(null, eval, null);
    }


    /**
     * 计算常规数学表达式（支持变量表达式）
     *
     * @param object 对象（可为空）
     * @param eval   数学表达式（支持变量表达式）
     * @return 计算结果
     */
    public static BigDecimal calc(Object object, String eval) throws Exception {
        return calc(object, eval, null);
    }

    /**
     * 计算常规数学表达式（支持变量表达式）
     *
     * @param object 对象（可为空）
     * @param eval   数学表达式（支持变量表达式）
     * @param varMap 变量结果（可为空）
     * @return 计算结果
     */
    public static BigDecimal calc(Object object, String eval, Map<String, Object> varMap) throws Exception {
        Calculator calculator = new Calculator(new StringReader(eval));
        calculator.setVarTrace(varMap != null);
        calculator.setObject(object);
        BigDecimal result = calculator.eval();
        if (varMap != null) {
            Map<String, Object> _varMap = calculator.getVarMap();
            if (_varMap != null) {
                varMap.putAll(_varMap);
            }
        }
        return result;
    }

}
