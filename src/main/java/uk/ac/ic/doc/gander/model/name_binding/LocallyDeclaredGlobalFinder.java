package uk.ac.ic.doc.gander.model.name_binding;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Global;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.NameTokType;
import org.python.pydev.parser.jython.ast.VisitorBase;

import uk.ac.ic.doc.gander.ast.LocalCodeBlockVisitor;

/**
 * Finds names declared global in the given code block.
 * 
 * This only finds names if the given code block contains the 'global' keyword
 * itself. A name <em>may still be</em> bind globally if a parent scope declares
 * it so but this class only looks at the given code block. If it is declared
 * global in a nested scope that also won't affect be registered as these
 * declarations don't affect global binding for their enclosing code blocks.
 * 
 * The 'global' statement can occur at any point in a code block but it's effect
 * covers the entire block (the spec says it must appear before the name is used
 * but CPython only generates a warning). Therefore we don't try to establish
 * that is precedes all mentions of the name.
 */
public abstract class LocallyDeclaredGlobalFinder extends VisitorBase {

    private boolean finished = false;

    /**
     * Implement to handle appearance of a 'global' declaration in the local
     * code block.
     * 
     * The implementation can end the search prematurely by returning {@code
     * true}. We can't return prematurely from the visitor so we use a flag to
     * know we're finished and short-cut the visitor's work.
     * 
     * @param name
     *            Name of declared global.
     * @return Whether the search is finished.
     */
    protected abstract boolean onGlobalDeclared(String name);

    @Override
    public final void traverse(SimpleNode node) throws Exception {
        // Do not traverse. We delegate visiting the tree to the inner visitor
    }

    @Override
    protected final Object unhandled_node(SimpleNode node) throws Exception {

        /*
         * Using a LocalCodeBlock visitor as a global statement in a nested
         * class or function doesn't affect the enclosing scope's binding
         */
        return node.accept(new LocalCodeBlockVisitor() {

            @Override
            public Object visitGlobal(Global node) throws Exception {
                if (!finished) {
                    for (NameTokType tok : node.names) {
                        finished = onGlobalDeclared(((NameTok) tok).id);
                        if (finished)
                            break;
                    }
                }
                return null;
            }

            @Override
            public void traverse(SimpleNode node) throws Exception {
                /*
                 * traverse by default because the 'global' statement might be
                 * nested, for instance, in a loop or conditional
                 */
                if (!finished) {
                    node.traverse(this);
                }
            }

            @Override
            protected Object unhandled_node(SimpleNode node) throws Exception {
                return null;
            }
        });
    }
}
