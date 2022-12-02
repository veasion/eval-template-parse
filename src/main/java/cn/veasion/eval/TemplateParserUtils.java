package cn.veasion.eval;

import cn.veasion.eval.tpl.BlockTemplateTree;
import cn.veasion.eval.tpl.ForTemplateTree;
import cn.veasion.eval.tpl.IfTemplateTree;
import cn.veasion.eval.tpl.TemplateTree;
import cn.veasion.eval.tpl.TokenEnum;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 模板解析工具类
 *
 * @author luozhuowei
 * @date 2021/11/30
 */
public class TemplateParserUtils {

    /**
     * 模板
     *
     * @param object      对象
     * @param inputStream 模板文件流
     * @return 模板结果
     */
    public static String parseTemplate(Object object, InputStream inputStream) throws Exception {
        return parseTemplate(object, inputStream, StandardCharsets.UTF_8);
    }

    /**
     * 模板
     *
     * @param object      对象
     * @param inputStream 模板文件流
     * @param encoding    字符编码
     * @return 模板结果
     */
    public static String parseTemplate(Object object, InputStream inputStream, Charset encoding) throws Exception {
        TemplateParser parser = new TemplateParser(inputStream, encoding);
        List<TemplateTree> treeList = parser.parse();
        EvalParser evalParser = new EvalParser(new StringReader(""));
        evalParser.setVarTrace(false);
        evalParser.setObject(object);
        StringBuilder sb = new StringBuilder();
        parseTemplate(object, evalParser, sb, treeList);
        return sb.toString();
    }

    private static void parseTemplate(Object object, EvalParser evalParser, StringBuilder sb, List<TemplateTree> treeList) throws Exception {
        if (treeList == null || treeList.isEmpty()) {
            return;
        }
        List<TemplateTree> list = merge(treeList);
        for (TemplateTree templateTree : list) {
            evalParser.setObject(object);
            TokenEnum tokenType = templateTree.getTokenType();
            if (TokenEnum.STRING.equals(tokenType) || TokenEnum.TRIM.equals(tokenType)) {
                sb.append(templateTree.getText());
            } else if (TokenEnum.EVAL.equals(tokenType)) {
                Object result = evalParser.eval(templateTree.getText());
                if (result != null) {
                    sb.append(result);
                }
            } else if (templateTree instanceof IfTemplateTree) {
                ifTemplateTree(object, evalParser, sb, (IfTemplateTree) templateTree);
            } else if (templateTree instanceof ForTemplateTree) {
                forTemplate(object, evalParser, sb, (ForTemplateTree) templateTree);
            }
        }
    }

    private static void forTemplate(Object object, EvalParser evalParser, StringBuilder sb, ForTemplateTree templateTree) throws Exception {
        Object evalResult = evalParser.eval(templateTree.getLeft());
        if (evalResult == null) {
            return;
        }
        appendPre(sb, templateTree);
        Map<String, Object> env = new HashMap<>();
        Function<String, ?> function = key -> {
            if (env.containsKey(key)) {
                return env.get(key);
            }
            return EvalObjectUtils.parseObject(object, key);
        };
        evalParser.setObject(function);
        if (evalResult instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) evalResult;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                env.put(templateTree.getRight1(), entry.getKey());
                if (templateTree.getRight2() != null && !"".equals(templateTree.getRight2())) {
                    env.put(templateTree.getRight2(), entry.getValue());
                }
                parseTemplate(function, evalParser, sb, templateTree.getChildren());
            }
        } else if (evalResult instanceof Iterable) {
            int index = 0;
            for (Object obj : (Iterable<?>) evalResult) {
                boolean one = templateTree.getRight2() == null || "".equals(templateTree.getRight2());
                if (one) {
                    env.put(templateTree.getRight1(), obj);
                } else {
                    env.put(templateTree.getRight1(), index++);
                    env.put(templateTree.getRight2(), obj);
                }
                parseTemplate(function, evalParser, sb, templateTree.getChildren());
            }
        } else {
            throw new RuntimeException("for > " + templateTree.getLeft() + " 变量类型不支持循环遍历");
        }
        appendEnd(sb, templateTree);
    }

    private static void ifTemplateTree(Object object, EvalParser evalParser, StringBuilder sb, IfTemplateTree templateTree) throws Exception {
        Object evalResult = evalParser.eval(templateTree.getEval());
        boolean ifResult = EvalObjectUtils.isTrue(evalResult);
        if (ifResult) {
            appendPre(sb, templateTree);
            parseTemplate(object, evalParser, sb, templateTree.getChildren());
        }
        IfTemplateTree elseIfTree = templateTree.getElseIfTree();
        if (!ifResult && elseIfTree != null) {
            evalResult = evalParser.eval(elseIfTree.getEval());
            ifResult = EvalObjectUtils.isTrue(evalResult);
            if (ifResult) {
                appendPre(sb, elseIfTree);
                parseTemplate(object, evalParser, sb, elseIfTree.getChildren());
            }
        }
        IfTemplateTree elseTree = templateTree.getElseTree();
        if (!ifResult && elseTree != null) {
            appendPre(sb, elseTree);
            parseTemplate(object, evalParser, sb, elseTree.getChildren());
        }
        if (ifResult || elseTree != null) {
            appendEnd(sb, templateTree);
        }
    }

    private static void appendPre(StringBuilder sb, BlockTemplateTree templateTree) {
        String pre = templateTree.pre();
        if (pre != null) {
            sb.append(pre);
        }
    }

    private static void appendEnd(StringBuilder sb, BlockTemplateTree templateTree) {
        String end = templateTree.end();
        if (end != null) {
            sb.append(end);
        }
    }

    private static List<TemplateTree> merge(List<TemplateTree> treeList) {
        if (treeList == null || treeList.isEmpty()) {
            return treeList;
        }
        List<TemplateTree> result = new ArrayList<>();
        for (int i = 0; i < treeList.size(); i++) {
            TemplateTree tree = treeList.get(i);
            if (TokenEnum.STRING.equals(tree.getTokenType())) {
                int lastIndex = i;
                StringBuilder sb = new StringBuilder();
                for (int j = i; j < treeList.size(); j++) {
                    TemplateTree temp = treeList.get(j);
                    if (!TokenEnum.STRING.equals(temp.getTokenType())) {
                        break;
                    }
                    lastIndex = j;
                    sb.append(temp.getText());
                }
                i = lastIndex;
                result.add(TemplateTree.build(TokenEnum.STRING, sb.toString()));
            } else {
                result.add(tree);
            }
        }
        return result;
    }

}
