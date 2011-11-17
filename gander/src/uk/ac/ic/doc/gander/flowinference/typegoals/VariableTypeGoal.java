package uk.ac.ic.doc.gander.flowinference.typegoals;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeConcentrator;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.Variable;
import uk.ac.ic.doc.gander.model.name_binding.Binder;
import uk.ac.ic.doc.gander.model.name_binding.NamespaceKey;

/**
 * Infers the type of a name in a code block.
 * 
 * Establishes the type by finding bindings to that name in the same binding
 * scope as this one. The binding scope is not the same thing as the enclosing
 * code block scope. They may be the same, for instance a local variable defined
 * and used in the same function, however, they may well be different such as a
 * global variable being bound in a non-module code block.
 * 
 * The search is flow, context and container insensitive as it treats the token
 * as a simple string rather than an identifier at a particular location, stack
 * frame, or allocated object.
 */
final class VariableTypeGoal implements TypeGoal {

	private final Variable variable;

	@Deprecated
	public VariableTypeGoal(Namespace enclosingScope, String tokenName) {
		this.variable = new Variable(tokenName, enclosingScope);
	}

	public VariableTypeGoal(Variable variable) {
		this.variable = variable;
	}

	public TypeJudgement initialSolution() {
		return SetBasedTypeJudgement.BOTTOM;
	}

	public TypeJudgement recalculateSolution(SubgoalManager goalManager) {

		return new VariableTypeGoalSolver(goalManager, variable).solution();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((variable == null) ? 0 : variable.hashCode());
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
		VariableTypeGoal other = (VariableTypeGoal) obj;
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.equals(other.variable))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "VariableTypeGoal [variable=" + variable + "]";
	}

}

/**
 * Handles solving the {@link VariableTypeGoal}.
 */
final class VariableTypeGoalSolver {
	private final SubgoalManager goalManager;
	private final NamespaceKey bindingLocation;
	private final TypeConcentrator type = new TypeConcentrator();

	VariableTypeGoalSolver(SubgoalManager goalManager, Variable variable) {
		this.goalManager = goalManager;
		this.bindingLocation = Binder.resolveBindingScope(variable);

		/*
		 * Namespace keys can only be bound to values in or below the
		 * namespace's code object, therefore we can use it as the root of the
		 * search rather than having to search from the top level of the model.
		 */
		addAssignmentsBelowCodeObject(bindingLocation.getNamespace());
	}

	TypeJudgement solution() {
		return type.getJudgement();
	}

	/**
	 * Add references to the name from all code objects contained in the given
	 * one.
	 * 
	 * @param codeObject
	 *            the root of the search
	 */
	private void addAssignmentsBelowCodeObject(Namespace codeObject) {

		// The name in the nested code block may not bind in the same place as
		// it does in the outer code block so we have to check
		Variable localVariable = new Variable(bindingLocation.getName(),
				codeObject);
		NamespaceKey localBindingLocation = Binder
				.resolveBindingScope(localVariable);

		if (localBindingLocation.equals(bindingLocation)) {
			// Ok, we're sure that the name in this code object is talking about
			// the same namespace location that we are interested in. So now
			// we want to know what this code object binds to it
			type.add(goalManager.registerSubgoal(new BoundTypeGoal(
					localVariable)));
		}

		/*
		 * Legally, only global names (i.e. ones whose binding location is their
		 * containing module) can be bound to a value outside the namespace of
		 * the code block they appear in so we make a shortcut here to save
		 * unecessary processing.
		 */
		if (!(bindingLocation.getNamespace() instanceof Module)) {
			return;
		}

		for (Namespace subCodeBlock : codeObject.getClasses().values()) {
			if (type.isFinished())
				return;

			addAssignmentsBelowCodeObject(subCodeBlock);
		}

		for (Namespace subCodeBlock : codeObject.getFunctions().values()) {
			if (type.isFinished())
				return;

			addAssignmentsBelowCodeObject(subCodeBlock);
		}
	}

}
