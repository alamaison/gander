package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ast.AstParentNodeFinder;
import uk.ac.ic.doc.gander.flowinference.ResultConcentrator;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.CodeObjectPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowGoal;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.NamespaceName;

/**
 * Infers the part of the type of a namespace name that comes from qualified
 * references to the name.
 * 
 * In other words, the part of the types that is implied by bindings to the name
 * via attribute access on an object.
 * 
 * This is not the complete type of the name as it doesn't include values bound
 * to the name by unqualified reference.
 */
final class QualifiedNameDefinitionsPartialSolution implements
		PartialTypeSolution {

	private final ResultConcentrator<Type> inferredType = new ResultConcentrator<Type>();
	private final SubgoalManager goalManager;

	public Set<Type> partialSolution() {
		return inferredType.result();
	}

	QualifiedNameDefinitionsPartialSolution(SubgoalManager goalManager,
			NamespaceName name) {
		assert goalManager != null;
		assert name != null;

		this.goalManager = goalManager;

		Set<ModelSite<? extends exprType>> namespacePositions = goalManager
				.registerSubgoal(new FlowGoal(new CodeObjectPosition(name
						.namespace())));

		Set<ModelSite<Attribute>> qualifiedReferences = new HashSet<ModelSite<Attribute>>();
		for (ModelSite<? extends exprType> expression : namespacePositions) {
			SimpleNode parent = AstParentNodeFinder.findParent(expression
					.astNode(), expression.codeObject().ast());
			if (parent instanceof Attribute
					&& ((NameTok) ((Attribute) parent).attr).id.equals(name
							.name())) {
				qualifiedReferences.add(new ModelSite<Attribute>(
						(Attribute) parent, expression.codeObject()));
			}
		}

		addBindingsReferences(qualifiedReferences);
	}

	private void addBindingsReferences(
			Set<ModelSite<Attribute>> qualifiedReferences) {

		for (ModelSite<Attribute> attribute : qualifiedReferences) {

			SimpleNode parent = AstParentNodeFinder.findParent(attribute
					.astNode(), attribute.codeObject().ast());

			// Check that attribute is being bound by assignment
			// FIXME: Attributes can be bound by any of the binding statements
			if (parent instanceof Assign
					&& Arrays.asList(((Assign) parent).targets).contains(
							attribute.astNode())) {

				ModelSite<exprType> rhs = new ModelSite<exprType>(
						((Assign) parent).value, attribute.codeObject());
				TypeJudgement rhsType = goalManager
						.registerSubgoal(new ExpressionTypeGoal(rhs));
				if (rhsType instanceof SetBasedTypeJudgement) {
					inferredType.add(((SetBasedTypeJudgement) rhsType)
							.getConstituentTypes());
				} else {
					inferredType.add(null);
				}

				if (inferredType.isTop())
					break;
			}
		}

	}

}
