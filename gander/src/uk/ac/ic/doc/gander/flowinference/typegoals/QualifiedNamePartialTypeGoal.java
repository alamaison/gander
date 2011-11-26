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
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.CodeObjectPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowGoal;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeConcentrator;
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
final class QualifiedNamePartialTypeGoal implements TypeGoal {

	private final NamespaceName name;

	public QualifiedNamePartialTypeGoal(NamespaceName name) {
		this.name = name;
	}

	public TypeJudgement initialSolution() {
		return SetBasedTypeJudgement.BOTTOM;
	}

	/**
	 * Establishes the type by finding bindings to the unqualified name in the
	 * same binding scope as this one. The binding scope is not the same thing
	 * as the enclosing code block. They may be the same, for instance a local
	 * name defined and used in the same function, however, they may well be
	 * different such as a global name being bound in a non-module code block.
	 * 
	 * The search is flow, context and container insensitive as it treats the
	 * token as a simple string rather than an identifier at a particular
	 * location, stack frame, or allocated object.
	 */
	public TypeJudgement recalculateSolution(SubgoalManager goalManager) {

		return new QualifiedNamePartialTypeGoalSolver(goalManager, name)
				.solution();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QualifiedNamePartialTypeGoal other = (QualifiedNamePartialTypeGoal) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "QualifiedNamePartialTypeGoal [name=" + name + "]";
	}

}

/**
 * Handles solving the {@link QualifiedNamePartialTypeGoal}.
 */
final class QualifiedNamePartialTypeGoalSolver {
	private final SubgoalManager goalManager;
	private final TypeConcentrator type = new TypeConcentrator();
	private final NamespaceName name;

	QualifiedNamePartialTypeGoalSolver(SubgoalManager goalManager,
			NamespaceName name) {
		this.goalManager = goalManager;
		this.name = name;

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
				type.add(goalManager
						.registerSubgoal(new ExpressionTypeGoal(rhs)));
				if (type.isFinished())
					break;
			}
		}

	}

	TypeJudgement solution() {
		return type.getJudgement();
	}

}
