// Autogenerated AST node
package org.python.pydev.parser.jython.ast;
import org.python.pydev.parser.jython.SimpleNode;

public final class With extends stmtType {
    public WithItemType[] with_item;
    public suiteType body;

    public With(WithItemType[] with_item, suiteType body) {
        this.with_item = with_item;
        this.body = body;
    }

    public With(WithItemType[] with_item, suiteType body, SimpleNode parent) {
        this(with_item, body);
        this.beginLine = parent.beginLine;
        this.beginColumn = parent.beginColumn;
    }

    public With createCopy() {
        WithItemType[] new0;
        if(this.with_item != null){
        new0 = new WithItemType[this.with_item.length];
        for(int i=0;i<this.with_item.length;i++){
            new0[i] = (WithItemType) (this.with_item[i] != null?
            this.with_item[i].createCopy():null);
        }
        }else{
            new0 = this.with_item;
        }
        With temp = new With(new0, body!=null?(suiteType)body.createCopy():null);
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
        StringBuffer sb = new StringBuffer("With[");
        sb.append("with_item=");
        sb.append(dumpThis(this.with_item));
        sb.append(", ");
        sb.append("body=");
        sb.append(dumpThis(this.body));
        sb.append("]");
        return sb.toString();
    }

    public Object accept(VisitorIF visitor) throws Exception {
        return visitor.visitWith(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (with_item != null) {
            for (int i = 0; i < with_item.length; i++) {
                if (with_item[i] != null){
                    with_item[i].accept(visitor);
                }
            }
        }
        if (body != null){
            body.accept(visitor);
        }
    }

}
