// Autogenerated AST node
package org.python.pydev.parser.jython.ast;
import org.python.pydev.parser.jython.SimpleNode;

public final class Name extends exprType implements expr_contextType {
    public String id;
    public int ctx;
    public boolean reserved;

    public Name(String id, int ctx, boolean reserved) {
        this.id = id;
        this.ctx = ctx;
        this.reserved = reserved;
    }

    public Name(String id, int ctx, boolean reserved, SimpleNode parent) {
        this(id, ctx, reserved);
        this.beginLine = parent.beginLine;
        this.beginColumn = parent.beginColumn;
    }

    public Name createCopy() {
        Name temp = new Name(id, ctx, reserved);
        temp.beginLine = this.beginLine;
        temp.beginColumn = this.beginColumn;
        if(this.specialsBefore != null){
            for(Object o:this.specialsBefore){
                if(o instanceof commentType){
                    commentType commentType = (commentType) o;
                    temp.getSpecialsBefore().add(commentType.createCopy());
                }
            }
        }
        if(this.specialsAfter != null){
            for(Object o:this.specialsAfter){
                if(o instanceof commentType){
                    commentType commentType = (commentType) o;
                    temp.getSpecialsAfter().add(commentType.createCopy());
                }
            }
        }
        return temp;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Name[");
        sb.append("id=");
        sb.append(dumpThis(this.id));
        sb.append(", ");
        sb.append("ctx=");
        sb.append(dumpThis(this.ctx, expr_contextType.expr_contextTypeNames));
        sb.append(", ");
        sb.append("reserved=");
        sb.append(dumpThis(this.reserved));
        sb.append("]");
        return sb.toString();
    }

    public Object accept(VisitorIF visitor) throws Exception {
        return visitor.visitName(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
    }

}
