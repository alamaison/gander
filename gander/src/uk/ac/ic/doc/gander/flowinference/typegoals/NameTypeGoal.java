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

		final Namespace bindingScope = new Binder().resolveBindingScope(name,
				enclosingScope);

		/*
		 * Other than global (module) names, a name can only be bound in the
		 * same scope as it is defined, i.e. its binding scope. The exception is
		 * if the scope is specified explicitly using an attribute to force a
		 * binding to mutate a particular namespace.
		 * 
		 * I think the attribute thing can only happen with Modules and Classes.
		 * Functions can't have their namespace modified using attribute access.
		 * There probably is a way (something like manipulating the function's
		 * dict attribute explicitly) but it would hardly be normal.
		 */
		// FIXME: We're ignoring the attribute exception

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
