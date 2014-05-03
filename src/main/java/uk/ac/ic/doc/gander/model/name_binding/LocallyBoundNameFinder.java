package uk.ac.ic.doc.gander.model.name_binding;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.VisitorBase;

/**
 * Find which names being bound in an AST limited to the local code block.
 * 
 * Halts traversal when encountering a {@link FunctionDef} or {@link ClassDef}
 * as, despite appearances, these are not part of the local code block. It is a
 * declaration of the nested class or function code block. Another way to think
 * about it: the nested body is not being 'executed' now whereas the enclosing
 * namespace's body is.
 * 
 * How to react when a name is found is left to the subclasses.
 */
public abstract class LocallyBoundNameFinder extends VisitorBase {

    protected abstract void onNameBound(String name);

    @Override
    public void traverse(SimpleNode node) throws Exception {
        // Do not traverse. We delegate visiting the tree to the inner
        // BoundNameVisitor
    }

    @Override
    protected Object unhandled_node(SimpleNode node) throws Exception {
        return node.accept(new BoundNameVisitor() {

            @Override
            protected void onNameBound(String name) {
                LocallyBoundNameFinder.this.onNameBound(name);
            }
        });
    }

}
