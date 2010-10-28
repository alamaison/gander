// Autogenerated AST node
package org.python.pydev.parser.jython.ast;
import org.python.pydev.parser.jython.SimpleNode;

public final class StrJoin extends exprType {
    public exprType[] strs;

    public StrJoin(exprType[] strs) {
        this.strs = strs;
    }

    public StrJoin(exprType[] strs, SimpleNode parent) {
        this(strs);
        this.beginLine = parent.beginLine;
        this.beginColumn = parent.beginColumn;
    }

    public StrJoin createCopy() {
        exprType[] new0;
        if(this.strs != null){
        new0 = new exprType[this.strs.length];
        for(int i=0;i<this.strs.length;i++){
            new0[i] = (exprType) (this.strs[i] != null? this.strs[i].createCopy():null);
        }
        }else{
            new0 = this.strs;
        }
        StrJoin temp = new StrJoin(new0);
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
        StringBuffer sb = new StringBuffer("StrJoin[");
        sb.append("strs=");
        sb.append(dumpThis(this.strs));
        sb.append("]");
        return sb.toString();
    }

    public Object accept(VisitorIF visitor) throws Exception {
        return visitor.visitStrJoin(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (strs != null) {
            for (int i = 0; i < strs.length; i++) {
                if (strs[i] != null){
                    strs[i].accept(visitor);
                }
            }
        }
    }

}
