package cn.veasion.eval;

import com.alibaba.fastjson.JSONObject;

import java.io.StringReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.function.Function;

/**
 * CalculatorTest
 *
 * @author luozhuowei
 * @date 2021/11/22
 */
public class CalculatorTest {

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
        JSONObject temp1 = JSONObject.parseObject("{\"a\":1,\"b\":2,\"c\":3,\"aa\":-1,\"bb\":-2,\"cc\":-3,\"d\":4.99,\"s\":8}");
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
        JSONObject temp2 = JSONObject.parseObject("{\"商品金额\": 10.25,\"销售数量\": 10,\"优惠金额\":2}");
        testCalculate(temp2, "商品金额*销售数量-优惠金额+默认值|5");

        System.out.println("======================");
        JSONObject temp3 = JSONObject.parseObject("{\"order\":{\"product_amt\":10.25,\"num\":10,\"product\":{\"user\":{\"age\":1,\"arr\":[10, 20, 30]}}}}");
        Function<String, ?> function = s -> {
            if ("random".equals(s)) {
                return Math.random();
            }
            return temp3.get(s);
        };
        testCalculate(function, "order.product_amt * order.num + random");
        testCalculate(function, "order.product_amt * order.num + random");
        testCalculate(function, "order.product.user['age'] + order.product.user.age + order.product.user.arr[0] + order.product.user.arr[order.product.user.age]");
    }

    private static void testCalculate(String eval) throws Exception {
        testCalculate(null, eval);
    }

    private static void testCalculate(Object object, String eval) throws Exception {
        Calculator parser = new Calculator(new StringReader(eval));
        parser.setObject(object);
        BigDecimal result = parser.eval();
        System.out.println(eval + "=" + decimalFormat(result, 2));
        System.out.println(parser.getVarMap());
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
