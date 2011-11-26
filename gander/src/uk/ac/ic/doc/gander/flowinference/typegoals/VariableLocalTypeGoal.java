package uk.ac.ic.doc.gander.flowinference.typegoals;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeConcentrator;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.Variable;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Binder;
import uk.ac.ic.doc.gander.model.name_binding.ScopedVariable;

/**
 * Partially infers the type of a variable implied by bindings to the variable.
 * 
 * This is not the complete type of the variable because a variable is just an
 * unqualified reference to a namespace name. The objects this name (and
 * therefore this variable) refers to can be bound using qualified references as
 * well.
 */
final class VariableLocalTypeGoal implements TypeGoal {

	private final Variable variable;

	@Deprecated
	public VariableLocalTypeGoal(Namespace enclosingScope, String tokenName) {
		this.variable = new Variable(tokenName, enclosingScope);
	}

	public VariableLocalTypeGoal(Variable variable) {
		this.variable = variable;
	}

	public TypeJudgement initialSolution() {
		return SetBasedTypeJudgement.BOTTOM;
	}

	/**
	 * Establishes the type by finding bindings to the variable in the same
	 * binding scope as this one. The binding scope is not the same thing as the
	 * enclosing code block. They may be the same, for instance a local variable
	 * defined and used in the same function, however, they may well be
	 * different such as a global variable being bound in a non-module code
	 * block.
	 * 
	 * The search is flow, context and container insensitive as it treats the
	 * token as a simple string rather than an identifier at a particular
	 * location, stack frame, or allocated object.
	 */
	public TypeJudgement recalculateSolution(SubgoalManager goalManager) {

		ScopedVariable resolvedVariable = Binder.resolveBindingScope(variable);

		return goalManager.registerSubgoal(new UnqualifiedNamePartialTypeGoal(
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
		VariableLocalTypeGoal other = (VariableLocalTypeGoal) obj;
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.equals(other.variable))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "VariableLocalTypeGoal [variable=" + variable + "]";
	}

}

/**
 * Handles solving the {@link VariableLocalTypeGoal}.
 */
final class VariableTypeGoalSolver {
	private final SubgoalManager goalManager;
	private final ScopedVariable bindingLocation;
	private final TypeConcentrator type = new TypeConcentrator();

	VariableTypeGoalSolver(SubgoalManager goalManager, Variable variable) {
		this.goalManager = goalManager;
		this.bindingLocation = Binder.resolveBindingScope(variable);

		TypeJudgement bob = goalManager
				.registerSubgoal(new UnqualifiedNamePartialTypeGoal(
						bindingLocation.bindingLocation()));

		/*
		 * Unqualified references to namespace names can only occur in or below
		 * the namespace's code object, therefore we can use it as the root of
		 * the search for bindings rather than having to search from the top
		 * level of the model.
		 */
		addBindingsBelowCodeObject(bindingLocation.bindingLocation()
				.namespace().codeObject());
	}

	TypeJudgement solution() {
		return type.getJudgement();
	}

	/**
	 * Add bindings to the variable from all code objects contained in the given
	 * one.
	 * 
	 * @param codeObject
	 *            the root of the search
	 */
	private void addBindingsBelowCodeObject(CodeObject codeObject) {

		// The name in the nested code block may not bind in the same place as
		// it does in the outer code block so we have to check
		Variable localVariable = new Variable(bindingLocation.getName(),
				codeObject);
		ScopedVariable localBindingLocation = Binder
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
		if (!(bindingLocation.bindingLocation().namespace() instanceof Module)) {
			return;
		}

		for (CodeObject subCodeBlock : codeObject.nestedCodeObjects()) {
			if (type.isFinished())
				return;

			addBindingsBelowCodeObject(subCodeBlock);
		}
	}

}
