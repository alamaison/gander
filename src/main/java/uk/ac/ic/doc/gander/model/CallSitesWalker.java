package uk.ac.ic.doc.gander.model;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.ast.LocalCodeBlockVisitor;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

/**
 * Walk over all call sites in the loaded model.
 * 
 * Reacts when it encounters a call site by calling the {@link EventHandler}
 * passed to the constructor.
 * 
 * Each call site consists of an AST node and its enclosing code object.
 */
public final class CallSitesWalker {

    public interface EventHandler {
        public void encounteredCallSite(Call call, CodeObject codeObject);
    }

    private final EventHandler eventHandler;

    public CallSitesWalker(Model model, EventHandler eventHandler) {
        this.eventHandler = eventHandler;

        new CodeObjectWalker() {
            @Override
            protected void visitCodeObject(CodeObject codeObject) {
                processCallsInCodeBlock(codeObject);
            }
        }.walk(model.getTopLevel().codeObject());
    }

    private void processCallsInCodeBlock(final CodeObject codeObject) {
        try {
            codeObject.codeBlock().accept(new LocalCodeBlockVisitor() {

                @Override
                public Object visitCall(Call node) throws Exception {
                    eventHandler.encounteredCallSite(node, codeObject);
                    return null;
                }

                @Override
                protected Object unhandled_node(SimpleNode node)
                        throws Exception {
                    return null;
                }

                @Override
                public void traverse(SimpleNode node) throws Exception {
                    node.traverse(this);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
