package cn.veasion.eval;

import java.io.StringReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * CalculatorTest
 *
 * @author luozhuowei
 * @date 2021/11/22
 */
public class CalculatorTest {

    static Calculator calculator = new Calculator(new StringReader(""));

    public static void main(String[] args) throws Exception {
        testCalculate("-2");
        testCalculate("√8");
        testCalculate("2^3");
        testCalculate("10%6");
        testCalculate("1+(-4)");
        testCalculate("√(3*3)");
        testCalculate("2×3÷3");
        testCalculate("1+3√(3*3*3)");
        testCalculate("2+5*2-6/3");
        testCalculate("(1+2+3)^2+4");
        testCalculate("2+5*(2-6*(3+1))/3");
        testCalculate("4.99+(5.99+6.99)*1.06^2+√4");

        System.out.println("======================");
        Map<String, Object> temp1 = new HashMap<String, Object>() {{
            put("a", 1);
            put("b", 2);
            put("c", 3);
            put("aa", -1);
            put("bb", -2);
            put("cc", -3);
            put("d", 4.99);
            put("s", 8);
        }};
        testCalculate(temp1, "-b");
        testCalculate(temp1, "√s");
        testCalculate(temp1, "b^c");
        testCalculate(temp1, "(s+b)%(c*b)");
        testCalculate(temp1, "a+(-4)");
        testCalculate(temp1, "√(c*c)");
        testCalculate(temp1, "b×c÷c");
        testCalculate(temp1, "a+c√(c*c*c)");
        testCalculate(temp1, "b+5*b-6/c");
        testCalculate(temp1, "(a+b+c)^b+4");
        testCalculate(temp1, "b+5*(b-6*(c+a))/c");
        testCalculate(temp1, "d+(5.99+6.99)*1.06^b+√(a+c)");

        System.out.println("======================");
        Map<String, Object> temp2 = new HashMap<String, Object>() {{
            put("商品金额", 10.25);
            put("销售数量", 10);
            put("优惠金额", 2);
        }};
        testCalculate(temp2, "商品金额*销售数量-优惠金额+默认值|5");

        System.out.println("======================");
        Map<String, Object> temp3 = new HashMap<String, Object>() {{
            put("order", new HashMap<String, Object>() {{
                put("num", 10);
                put("field", "num");
                put("product_amt", 10.25);
                put("product", new HashMap<String, Object>() {{
                    put("user", new HashMap<String, Object>() {{
                        put("age", 18);
                        put("array", new int[]{10, 20, 30});
                    }});
                }});
            }});
            put("ext", (Function<String, ?>) s -> {
                if ("random".equals(s)) {
                    return Math.random();
                }
                if ("date".equals(s)) {
                    return System.currentTimeMillis();
                }
                return null;
            });
        }};
        testCalculate(temp3, "order[order.field] * 2");
        testCalculate(temp3, "order.product_amt * order.num + ext.random");
        testCalculate(temp3, "order.product_amt * order.num + ext.random");
        testCalculate(temp3, String.format("ext.date - %d", System.currentTimeMillis()));
        testCalculate(temp3, "order.product.user['age'] + order.product.user.age");
        testCalculate(temp3, "order.product.user.array[0] + order.product.user.array[order.product.user.age]");
    }

    private static void testCalculate(String eval) throws Exception {
        testCalculate(null, eval);
    }

    private static synchronized void testCalculate(Object object, String eval) throws Exception {
        BigDecimal result = calculator.eval(object, eval);
        System.out.println("计算：" + eval + "=" + decimalFormat(result, 2));
        if (object != null) {
            System.out.println("变量集：" + calculator.getVarMap());
        }
    }

    public static String decimalFormat(BigDecimal value, int n) {
        if (value == null) {
            return null;
        }
        String pattern = n > 0 ? "#." : "#";
        for (int i = 0; i < n && n <= 10; i++) {
            pattern = pattern.concat("#");
        }
        return new DecimalFormat(pattern).format(value);
    }

}
