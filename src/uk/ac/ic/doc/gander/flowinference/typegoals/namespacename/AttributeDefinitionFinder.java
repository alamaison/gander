package uk.ac.ic.doc.gander.flowinference.typegoals.namespacename;

import java.util.Arrays;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ast.AstParentNodeFinder;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * Finder of any operations setting the value of a given set of attribute.
 * 
 * Basically, filters the attributes to just those being defined and finds the
 * value they are being defined as.
 */
final class AttributeDefinitionFinder {

	interface Event {

		/**
		 * Attribute is being defined.
		 * 
		 * @param attribute
		 *            the attribute in question
		 * @param value
		 *            the expression it is being defined as
		 * @return whether the search is finished; {@code true} to stop the
		 *         search, {@code false} to continue it.
		 */
		boolean attributeDefined(ModelSite<Attribute> attribute,
				ModelSite<exprType> value);
	}

	AttributeDefinitionFinder(Set<ModelSite<Attribute>> attributes,
			Event eventHandler) {
		for (ModelSite<Attribute> attribute : attributes) {

			SimpleNode parent = AstParentNodeFinder.findParent(attribute
					.astNode(), attribute.codeObject().ast());

			// FIXME: Attributes can be bound by any of the binding statements
			if (parent instanceof Assign
					&& Arrays.asList(((Assign) parent).targets).contains(
							attribute.astNode())) {

				ModelSite<exprType> rhs = new ModelSite<exprType>(
						((Assign) parent).value, attribute.codeObject());
				if (eventHandler.attributeDefined(attribute, rhs))
					break;
			}
		}

	}

}
