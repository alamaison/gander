package uk.ac.ic.doc.gander.model;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ast.LocalCodeBlockVisitor;

/**
 * Walk over all assignments that write to an attribute of the given name in the
 * loaded model.
 * 
 * Reacts when it encounters a matching attribute being written by calling the
 * {@link EventHandler} passed to the constructor.
 * 
 * Each attribute site consists of an AST node and its enclosing model
 * namespace.
 */
public final class AttributeAssignmentSitesWalker {

	public interface EventHandler {
		public void encounteredAttributeAssignment(Assign write,
				Attribute attribute, Namespace namespace);
	}

	private final EventHandler eventHandler;

	public AttributeAssignmentSitesWalker(Model model, EventHandler eventHandler) {
		this.eventHandler = eventHandler;

		new CodeObjectWalker() {
			@Override
			protected void visitCodeObject(Namespace codeBlock) {
				processAttributeWritesInCodeBlock(codeBlock);
			}
		}.walk(model.getTopLevel());
	}

	private void processAttributeWritesInCodeBlock(final Namespace namespace) {
		try {
			namespace.asCodeBlock().accept(new LocalCodeBlockVisitor() {

				@Override
				public Object visitAssign(Assign node) throws Exception {
					for (exprType target : node.targets) {
						if (target instanceof Attribute) {
							eventHandler.encounteredAttributeAssignment(node,
									(Attribute) target, namespace);
						}
					}
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
