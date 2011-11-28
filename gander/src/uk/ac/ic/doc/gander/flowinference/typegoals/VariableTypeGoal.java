package uk.ac.ic.doc.gander.flowinference.typegoals;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.model.Variable;
import uk.ac.ic.doc.gander.model.name_binding.Binder;
import uk.ac.ic.doc.gander.model.name_binding.ScopedVariable;

/**
 * Infers the type of a named variable in a code block.
 * 
 * The inference is flow, context and container insensitive as it treats the
 * token as a simple string rather than an identifier at a particular location,
 * stack frame, or allocated object.
 */
final class VariableTypeGoal implements TypeGoal {

	private final Variable variable;

	public VariableTypeGoal(Variable variable) {
		this.variable = variable;
	}

	public TypeJudgement initialSolution() {
		return SetBasedTypeJudgement.BOTTOM;
	}

	/**
	 * Resolves the variable to the namespace in which it binds and delegates
	 * the type inference to a type goal for that namespace name.
	 */
	public TypeJudgement recalculateSolution(SubgoalManager goalManager) {

		ScopedVariable resolvedVariable = Binder.resolveBindingScope(variable);

		return goalManager.registerSubgoal(new NamespaceNameTypeGoal(
				resolvedVariable.bindingLocation()));
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
