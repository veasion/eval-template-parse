package cn.veasion.eval.tpl;

/**
 * IfTemplateTree
 *
 * @author luozhuowei
 * @date 2021/11/29
 */
public class IfTemplateTree extends BlockTemplateTree {

    private String eval;
    private IfTemplateTree elseIfTree;
    private IfTemplateTree elseTree;

    public static IfTemplateTree build(TokenEnum tokenType, String eval) {
        IfTemplateTree tree = new IfTemplateTree();
        tree.setTokenType(tokenType);
        tree.eval = eval.trim();
        return tree;
    }

    public String getEval() {
        return eval;
    }

    public void setEval(String eval) {
        this.eval = eval;
    }

    public IfTemplateTree getElseIfTree() {
        return elseIfTree;
    }

    public void setElseIfTree(IfTemplateTree elseIfTree) {
        this.elseIfTree = elseIfTree;
    }

    public IfTemplateTree getElseTree() {
        return elseTree;
    }

    public void setElseTree(IfTemplateTree elseTree) {
        this.elseTree = elseTree;
    }
}
