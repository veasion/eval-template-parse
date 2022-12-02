package cn.veasion.eval;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * EvalParserTest
 *
 * @author luozhuowei
 * @date 2021/11/25
 */
public class EvalParserTest {

    public static void main(String[] args) throws Exception{
        Map<String, Object> object = new HashMap<String, Object>() {{
            put("order", new HashMap<String, Object>() {{
                put("num", 100);
                put("field", "num");
                put("product_amt", 20.25);
                put("product", new HashMap<String, Object>() {{
                    put("user", new HashMap<String, Object>() {{
                        put("age", 18);
                        put("index", 1);
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
        testEval(object, "order.num");
        testEval(object, "order['num']");
        testEval(object, "order[order.field]");
        testEval(object, "order.product_amt");
        testEval(object, "ext.random");
        testEval(object, "order.product.user['age']");
        testEval(object, "order.product.user.array[order.product.user.index]");
        testEval(object, "order.field == 'num' && 1=='1'");
        testEval(object, "order[order.field] == 10 && 1 eq 1 && 1 == 2");
        testEval(object, "(!(false))");
        testEval(object, "order[order.field] == 10 ? (1 != 2 ? order.field : order.product.user.age) : 'xxx'");
        testEval(object, "order.product.user.age > 18 || order.field != 'num'");
        testEval(object, "order.product");
        testEvalReplace(object, "随机数：${ext.random}");
        testEvalReplace(object, "默认：${ext.abc|order.bb|order.field|10}");
        testEvalReplace(object, "订单数量：${order.num}，用户年龄：${order.product.user.age}，其他：${1>2?'x':(!(order!=null)?'xx':'xxx')}。");
    }

    private static void testEval(Object object, String eval) throws Exception {
        Map<String, Object> varMap = new HashMap<>();
        Object result = EvalParserUtils.eval(object, eval, varMap);
        System.out.println("表达式> " + eval);
        System.out.println("变量值> " + varMap);
        System.out.println("结果> " + result);
        System.out.println();
    }

    private static void testEvalReplace(Object object, String str) throws Exception {
        Map<String, Object> varMap = new HashMap<>();
        Object result = EvalParserUtils.template(object, str, varMap);
        System.out.println("字符串> " + str);
        System.out.println("变量值> " + varMap);
        System.out.println("结果> " + result);
        System.out.println();
    }

}
