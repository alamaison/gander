package uk.ac.ic.doc.gander.flowinference.typegoals;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.name_binding.Binder;

/**
 * Infers the type of a name within a code block.
 * 
 * Establishes the type by finding bindings to that name in the same binding
 * scope as this one. The binding scope is not the same thing as the enclosing
 * scope. They may be the same, for instance a local variable defined and used
 * in the same function, however, they may well be different such as a global
 * variable being bound in a non-module code block.
 * 
 * The search is flow, context and container insensitive as it treats the token
 * as a simple string rather than an identifier at a particular location, stack
 * frame, or allocated object.
 */
final class NameTypeGoal implements TypeGoal {

	private final Model model;
	private final Namespace enclosingScope;
	private final String name;

	public NameTypeGoal(Model model, Namespace enclosingScope, String tokenName) {
		this.model = model;
		this.enclosingScope = enclosingScope;
		this.name = tokenName;
	}

	public TypeJudgement initialSolution() {
		return SetBasedTypeJudgement.BOTTOM;
	}

	public TypeJudgement recalculateSolution(SubgoalManager goalManager) {

		/**
		 * FIXME: This is completely wrong. We only look for bindings in the
		 * binding scope of the name. But this scope only limits the binding we
		 * should consider to binding that target the same scope. It doesn't
		 * mean that we should only consider bindings occurring directly in that
		 * scope.
		 * 
		 * For instance, what about the case where the first assignment to the
		 * variable is in a *sibling* scope. For instance, a global variable
		 * that is initialised through an {@code init()} method. This search
		 * wouldn't find the assignment that happens in the body of {@code
		 * init()}. This won't give the *wrong* answer, as we eventually fail to
		 * find a typing leading to a judgement of Top, unless there is an
		 * assignment in an enclosing namespace *and* in a sibling and these
		 * assignments assign different types. In this case, the inferred type
		 * will be too narrow, it will only include the type in the enclosing
		 * namespace rather than leading to a union type.
		 */
		final Namespace bindingScope = new Binder().resolveBindingScope(name,
				enclosingScope);

		return goalManager.registerSubgoal(new BoundTypeGoal(model,
				bindingScope, name));

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((enclosingScope == null) ? 0 : enclosingScope.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof NameTypeGoal))
			return false;
		NameTypeGoal other = (NameTypeGoal) obj;
		if (enclosingScope == null) {
			if (other.enclosingScope != null)
				return false;
		} else if (!enclosingScope.equals(other.enclosingScope))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
