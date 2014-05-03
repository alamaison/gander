package uk.ac.ic.doc.gander.model;

import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

/**
 * Visit each code object in the model.
 * 
 * Like {@link NamespaceWalker} but treats all model elements uniformly.
 */
public abstract class CodeObjectWalker {

    public final void walk(CodeObject root) {
        visitCodeObject(root);
        walkThroughNestedCodeObjects(root);
    }

    private void walkThroughNestedCodeObjects(CodeObject codeObject) {
        for (CodeObject module : codeObject.nestedCodeObjects()) {
            walk(module);
        }
    }

    /**
     * Triggered on encountering an enclosed code object element.
     */
    protected abstract void visitCodeObject(CodeObject codeObject);
}
