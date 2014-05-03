package uk.ac.ic.doc.gander.model;

import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ast.AstParentNodeFinder;

/**
 * Given a set of expressions, find any attribute accesses on those expressions.
 */
public final class AttributeAccessFinder {

	public interface Event {
		boolean attributeAccess(ModelSite<Attribute> access);
	}

	public AttributeAccessFinder(
			Set<? extends ModelSite<? extends exprType>> objectPositions,
			Event eventHandler) {
		assert objectPositions != null;

		for (ModelSite<? extends exprType> object : objectPositions) {

			SimpleNode parent = AstParentNodeFinder.findParent(
					object.astNode(), object.codeObject().ast());
			if (parent instanceof Attribute) {
				eventHandler.attributeAccess(new ModelSite<Attribute>(
						(Attribute) parent, object.codeObject()));
			}

		}
	}
}
