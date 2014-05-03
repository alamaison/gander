package uk.ac.ic.doc.gander.model.codeobject;

public interface NamedCodeObject extends CodeObject {

    /**
     * Return the name the code object was declared with.
     * 
     * This is not the same as the name it is used by as it can be rebound to
     * other names arbitrarily.
     * 
     * @return the name given in the code object's declaration
     */
    String declaredName();
}
