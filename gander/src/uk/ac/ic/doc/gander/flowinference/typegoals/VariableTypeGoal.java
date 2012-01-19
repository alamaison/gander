package uk.ac.ic.doc.gander.flowinference.typegoals;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

/**
 * Infers the type of a named variable in a code block.
 * 
 * The inference is flow, context and container insensitive as it treats the
 * token as a simple string rather than an identifier at a particular location,
 * stack frame, or allocated object.
 */
public final class VariableTypeGoal implements TypeGoal {

	private final Variable variable;

	public VariableTypeGoal(Variable variable) {
		this.variable = variable;
	}

	public Result<Type> initialSolution() {
		return FiniteResult.bottom();
	}

	/**
	 * Resolves the variable to the namespace in which it binds and delegates
	 * the type inference to a type goal for that namespace name.
	 */
	public Result<Type> recalculateSolution(SubgoalManager goalManager) {

		return goalManager.registerSubgoal(new NamespaceNameTypeGoal(
				new NamespaceName(variable.bindingLocation())));
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
