package cn.veasion.eval.tpl;

/**
 * ForTemplateTree
 *
 * @author luozhuowei
 * @date 2021/11/29
 */
public class ForTemplateTree extends BlockTemplateTree {

    private String left;
    private String right1;
    private String right2;

    public static ForTemplateTree build(TokenEnum tokenType, String eval) {
        ForTemplateTree tree = new ForTemplateTree();
        tree.setTokenType(tokenType);
        String[] split = eval.split("\\s+as\\s+");
        tree.left = split[0].trim();
        String right = split[1].trim();
        String[] splits = right.split(",");
        tree.right1 = splits[0].trim();
        if (splits.length > 1) {
            tree.right2 = splits[1].trim();
        }
        return tree;
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getRight1() {
        return right1;
    }

    public void setRight1(String right1) {
        this.right1 = right1;
    }

    public String getRight2() {
        return right2;
    }

    public void setRight2(String right2) {
        this.right2 = right2;
    }
}
