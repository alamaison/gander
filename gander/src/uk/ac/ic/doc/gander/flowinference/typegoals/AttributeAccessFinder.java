package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ast.AstParentNodeFinder;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * Given a set of expressions, find any attribute accesses on those expressions.
 */
final class AttributeAccessFinder {

	interface Event {
		boolean attributeAccess(ModelSite<Attribute> access);
	}

	AttributeAccessFinder(Set<ModelSite<? extends exprType>> objectPositions,
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
