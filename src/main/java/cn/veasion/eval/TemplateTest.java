package cn.veasion.eval;

import cn.veasion.eval.tpl.TemplateParserUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * TemplateTest
 *
 * @author luozhuowei
 * @date 2021/11/29
 */
public class TemplateTest {

    public static void main(String[] args) throws Exception {
        InputStream inputStream = TemplateTest.class.getResourceAsStream("/test.template");

        Map<String, Object> object = new HashMap<>();
        object.put("name", "Veasion");
        object.put("sex", "男");
        object.put("age", 0);
        object.put("users", new ArrayList<Object>(){{
            add(new HashMap<String,Object>(){{
                put("name", "用户1");
                put("attrMap", new HashMap<String, Object>(){{
                    put("颜色", "红色");
                    put("爱好", "打球");
                    put("", "多大");
                }});
            }});
            add(new HashMap<String,Object>(){{
                put("name", "用户2");
                put("attrMap", new HashMap<String, Object>(){{
                    put("颜色", "白色");
                    put("喜欢", "睡觉");
                    put("哈哈", "");
                }});
            }});
        }});

        String template = TemplateParserUtils.parseTemplate(object, inputStream);
        System.out.println(template);
    }

}
