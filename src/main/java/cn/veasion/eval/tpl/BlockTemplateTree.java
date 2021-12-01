package cn.veasion.eval.tpl;

import cn.veasion.eval.Token;

/**
 * BlockTemplateTree
 *
 * @author luozhuowei
 * @date 2021/12/1
 */
public class BlockTemplateTree extends TemplateTree {

    private String pre;
    private String next;
    private String end;

    public String pre() {
        String line;
        if (pre != null && (line = line(pre)) != null && next != null && line(next) != null) {
            return line;
        }
        return pre;
    }

    public String end() {
        if (end == null) {
            return null;
        }
        String line = line(end);
        if (line != null && line(end.substring(end.indexOf(line) + line.length())) != null) {
            return line;
        }
        return end;
    }

    public TemplateTree setPreNextToken(Token preToken, Token nextToken) {
        pre = preToken.image.substring(0, preToken.image.indexOf("<#"));
        next = nextToken.image.substring(nextToken.image.indexOf(">") + 1);
        return this;
    }

    public TemplateTree setPreNextToken(Token token) {
        pre = token.image.substring(0, token.image.indexOf("<"));
        next = token.image.substring(token.image.indexOf(">") + 1);
        return this;
    }

    public TemplateTree setEndToken(Token token) {
        end = token.image.substring(0, token.image.indexOf("<")) + token.image.substring(token.image.indexOf(">") + 1);
        return this;
    }

    private String line(String str) {
        if (str.contains("\r\n")) {
            return "\r\n";
        } else if (str.contains("\n")) {
            return "\n";
        } else if (str.contains("\r")) {
            return "\r";
        }
        return null;
    }

    public String getPre() {
        return pre;
    }

    public void setPre(String pre) {
        this.pre = pre;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}
