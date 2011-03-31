// Autogenerated AST node
package org.python.pydev.parser.jython.ast;
import org.python.pydev.parser.jython.SimpleNode;

public final class excepthandlerType extends SimpleNode {
    public exprType type;
    public exprType name;
    public stmtType[] body;

    public excepthandlerType(exprType type, exprType name, stmtType[] body) {
        this.type = type;
        this.name = name;
        this.body = body;
    }

    public excepthandlerType(exprType type, exprType name, stmtType[] body, SimpleNode parent) {
        this(type, name, body);
        this.beginLine = parent.beginLine;
        this.beginColumn = parent.beginColumn;
    }

    public excepthandlerType createCopy() {
        stmtType[] new0;
        if(this.body != null){
        new0 = new stmtType[this.body.length];
        for(int i=0;i<this.body.length;i++){
            new0[i] = (stmtType) (this.body[i] != null? this.body[i].createCopy():null);
        }
        }else{
            new0 = this.body;
        }
        excepthandlerType temp = new excepthandlerType(type!=null?(exprType)type.createCopy():null,
        name!=null?(exprType)name.createCopy():null, new0);
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
        StringBuffer sb = new StringBuffer("excepthandler[");
        sb.append("type=");
        sb.append(dumpThis(this.type));
        sb.append(", ");
        sb.append("name=");
        sb.append(dumpThis(this.name));
        sb.append(", ");
        sb.append("body=");
        sb.append(dumpThis(this.body));
        sb.append("]");
        return sb.toString();
    }

    public Object accept(VisitorIF visitor) throws Exception {
        traverse(visitor);
        return null;
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (type != null){
            type.accept(visitor);
        }
        if (name != null){
            name.accept(visitor);
        }
        if (body != null) {
            for (int i = 0; i < body.length; i++) {
                if (body[i] != null){
                    body[i].accept(visitor);
                }
            }
        }
    }

}