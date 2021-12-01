package cn.veasion.eval.tpl;

import java.util.List;

/**
 * TemplateTree
 *
 * @author luozhuowei
 * @date 2021/11/29
 */
public class TemplateTree {

    private TokenEnum tokenType;
    private String text;
    private List<TemplateTree> children;

    public static TemplateTree build(TokenEnum tokenType, String text) {
        TemplateTree tree = new TemplateTree();
        tree.tokenType = tokenType;
        tree.text = text;
        return tree;
    }

    public TokenEnum getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenEnum tokenType) {
        this.tokenType = tokenType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<TemplateTree> getChildren() {
        return children;
    }

    public void setChildren(List<TemplateTree> children) {
//        if (children != null && !children.isEmpty()) {
//            for (TemplateTree tree : children) {
//                if (!TokenEnum.TRIM.equals(tree.getTokenType())) {
//                    break;
//                }
//                tree.setText("");
//            }
//        }
        this.children = children;
    }
}
