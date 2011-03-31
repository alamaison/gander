// Autogenerated AST node
package org.python.pydev.parser.jython.ast;
import org.python.pydev.parser.jython.SimpleNode;

public final class AugAssign extends stmtType implements operatorType {
    public exprType target;
    public int op;
    public exprType value;

    public AugAssign(exprType target, int op, exprType value) {
        this.target = target;
        this.op = op;
        this.value = value;
    }

    public AugAssign(exprType target, int op, exprType value, SimpleNode parent) {
        this(target, op, value);
        this.beginLine = parent.beginLine;
        this.beginColumn = parent.beginColumn;
    }

    public AugAssign createCopy() {
        AugAssign temp = new AugAssign(target!=null?(exprType)target.createCopy():null, op,
        value!=null?(exprType)value.createCopy():null);
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
        StringBuffer sb = new StringBuffer("AugAssign[");
        sb.append("target=");
        sb.append(dumpThis(this.target));
        sb.append(", ");
        sb.append("op=");
        sb.append(dumpThis(this.op, operatorType.operatorTypeNames));
        sb.append(", ");
        sb.append("value=");
        sb.append(dumpThis(this.value));
        sb.append("]");
        return sb.toString();
    }

    public Object accept(VisitorIF visitor) throws Exception {
        return visitor.visitAugAssign(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (target != null){
            target.accept(visitor);
        }
        if (value != null){
            value.accept(visitor);
        }
    }

}