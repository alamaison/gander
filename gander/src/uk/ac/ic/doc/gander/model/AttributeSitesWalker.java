package uk.ac.ic.doc.gander.model;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Attribute;

import uk.ac.ic.doc.gander.ast.LocalCodeBlockVisitor;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

/**
 * Walk over all attribute references in the loaded model.
 * 
 * Reacts when it encounters an attribute by calling the {@link EventHandler}
 * passed to the constructor.
 * 
 * Each attribute consists of an AST node and its enclosing code object.
 */
public final class AttributeSitesWalker {

	public interface EventHandler {
		public void encounteredAttribute(Attribute attribute,
				CodeObject codeObject);
	}

	private final EventHandler eventHandler;

	public AttributeSitesWalker(Model model, EventHandler eventHandler) {
		this.eventHandler = eventHandler;

		new CodeObjectWalker() {
			@Override
			protected void visitCodeObject(CodeObject codeObject) {
				processAttributesInCodeBlock(codeObject);
			}
		}.walk(model.getTopLevel().codeObject());
	}

	private void processAttributesInCodeBlock(final CodeObject codeObject) {
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
