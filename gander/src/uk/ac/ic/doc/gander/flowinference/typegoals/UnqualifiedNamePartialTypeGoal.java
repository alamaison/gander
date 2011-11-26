package uk.ac.ic.doc.gander.flowinference.typegoals;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeConcentrator;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.Variable;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Binder;
import uk.ac.ic.doc.gander.model.name_binding.ScopedVariable;

/**
 * Infers the part of the type of a namespace name that comes from unqualified
 * references to the name.
 * 
 * In other words, the part of the types that is implied by bindings to the name
 * the name that appear as variables in the scope of the namespace.
 * 
 * This is not the complete type of the name as it doesn't include values bound
 * to the name by qualified reference.
 */
final class UnqualifiedNamePartialTypeGoal implements TypeGoal {

	private final NamespaceName name;

	public UnqualifiedNamePartialTypeGoal(NamespaceName name) {
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

		return new UnqualifiedNamePartialTypeGoalSolver(goalManager, name)
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
		UnqualifiedNamePartialTypeGoal other = (UnqualifiedNamePartialTypeGoal) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "UnqualifiedNamePartialTypeGoal [name=" + name + "]";
	}

}

/**
 * Handles solving the {@link UnqualifiedNamePartialTypeGoal}.
 */
final class UnqualifiedNamePartialTypeGoalSolver {
	private final SubgoalManager goalManager;
	private final TypeConcentrator type = new TypeConcentrator();
	private final NamespaceName name;

	UnqualifiedNamePartialTypeGoalSolver(SubgoalManager goalManager,
			NamespaceName name) {
		this.goalManager = goalManager;
		this.name = name;

		/*
		 * There is no need to need to resolve the binding location. We have
		 * been given the namespace in the NamespaceName.
		 */

		/*
		 * Unqualified references to namespace names can only occur in or below
		 * the namespace's code object, therefore we can use it as the root of
		 * the search for bindings rather than having to search from the top
		 * level of the model.
		 */
		addUnqualifiedBindingsBelowCodeObject(name.namespace().codeObject());
	}

	TypeJudgement solution() {
		return type.getJudgement();
	}

	/**
	 * Add bindings to the name from all code objects contained in the given
	 * one.
	 * 
	 * @param codeObject
	 *            the root of the search
	 */
	private void addUnqualifiedBindingsBelowCodeObject(CodeObject codeObject) {

		/*
		 * The name in the code block may not bind in the same namespace as the
		 * namespace name we have been given so we have to check
		 */
		Variable localVariable = new Variable(name.name(), codeObject);
		ScopedVariable localBindingLocation = Binder
				.resolveBindingScope(localVariable);

		if (localBindingLocation.bindingLocation().equals(name)) {
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
		 * unnecessary processing.
		 */
		if (!(name.namespace() instanceof Module)) {
			return;
		}

		for (CodeObject nestedCodeObject : codeObject.nestedCodeObjects()) {
			if (type.isFinished())
				return;

			addUnqualifiedBindingsBelowCodeObject(nestedCodeObject);
		}
	}

}

