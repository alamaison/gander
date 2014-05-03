package uk.ac.ic.doc.gander.model.codeobject;

public interface NestedCodeObject extends CodeObject {

    /**
     * Returns the parent code object in which this code object appears
     */
    CodeObject parent();

}
