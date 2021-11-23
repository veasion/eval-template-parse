package cn.veasion.eval;

import java.io.StringReader;
import java.math.BigDecimal;

/**
 * Test
 *
 * @author luozhuowei
 * @date 2021/11/22
 */
public class Test {

    public static void main(String[] args) throws Exception {
        String eval = "1.5+(2.5*2)+3+10/2-4";
        Calculator parser = new Calculator(new StringReader(eval));
        BigDecimal result = parser.eval();
        System.out.println(eval + "=" + result);
    }

}
