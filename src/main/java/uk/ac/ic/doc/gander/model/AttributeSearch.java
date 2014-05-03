package uk.ac.ic.doc.gander.model;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Attribute;

import uk.ac.ic.doc.gander.ast.LocalCodeBlockVisitor;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

/**
 * Walk over all attribute references in a code object's code block.
 * 
 * Reacts when it encounters an attribute by calling the {@link EventHandler}
 * passed to the constructor.
 * 
 * Each attribute consists of an AST node and its enclosing code object.
 */
public final class AttributeSearch {

    public interface EventHandler {
        public void encounteredAttribute(Attribute attribute,
                CodeObject codeObject);
    }

    public AttributeSearch(final CodeObject codeObject,
            final EventHandler eventHandler) {

        try {
            codeObject.codeBlock().accept(new LocalCodeBlockVisitor() {

                @Override
                public Object visitAttribute(Attribute node) throws Exception {
                    eventHandler.encounteredAttribute(node, codeObject);
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
