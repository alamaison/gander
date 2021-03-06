// Autogenerated AST node
package org.python.pydev.parser.jython.ast;
import org.python.pydev.parser.jython.SimpleNode;

public final class aliasType extends SimpleNode {
    public NameTokType name;
    public NameTokType asname;

    public aliasType(NameTokType name, NameTokType asname) {
        this.name = name;
        this.asname = asname;
    }

    public aliasType(NameTokType name, NameTokType asname, SimpleNode parent) {
        this(name, asname);
        this.beginLine = parent.beginLine;
        this.beginColumn = parent.beginColumn;
    }

    public aliasType createCopy() {
        aliasType temp = new aliasType(name!=null?(NameTokType)name.createCopy():null,
        asname!=null?(NameTokType)asname.createCopy():null);
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
        StringBuffer sb = new StringBuffer("alias[");
        sb.append("name=");
        sb.append(dumpThis(this.name));
        sb.append(", ");
        sb.append("asname=");
        sb.append(dumpThis(this.asname));
        sb.append("]");
        return sb.toString();
    }

    public Object accept(VisitorIF visitor) throws Exception {
        traverse(visitor);
        return null;
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (name != null){
            name.accept(visitor);
        }
        if (asname != null){
            asname.accept(visitor);
        }
    }

}
