package uk.ac.ic.doc.gander.flowinference.typegoals;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

/**
 * Infers the part of the type of a namespace name that comes from unqualified
 * references to the name.
 * 
 * In other words, the part of the types that is implied by bindings to the name
 * the name that appear as variables in the scope of the namespace.
 * 
 * This is not the complete type of the name as it doesn't include values bound
 * to the name by qualified reference.
 * 
 * Establishes the type by finding bindings to the unqualified name in the same
 * binding scope as this one. The binding scope is not the same thing as the
 * enclosing code block. They may be the same, for instance a local name defined
 * and used in the same function, however, they may well be different such as a
 * global name being bound in a non-module code block.
 * 
 * The search is flow, context and container insensitive as it treats the token
 * as a simple string rather than an identifier at a particular location, stack
 * frame, or allocated object.
 */
final class UnqualifiedNameDefinitionsPartialSolution implements
		PartialTypeSolution {

	private final RedundancyEliminator<Type> inferredType = new RedundancyEliminator<Type>();
	private final SubgoalManager goalManager;
	private final NamespaceName name;

	public Result<Type> partialSolution() {
		return inferredType.result();
	}

	UnqualifiedNameDefinitionsPartialSolution(SubgoalManager goalManager,
			NamespaceName name) {
		assert goalManager != null;
		assert name != null;

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

		if (localVariable.bindingLocation().equals(name)) {
			// Ok, we're sure that the name in this code object is talking about
			// the same namespace location that we are interested in. So now
			// we want to know what this code object binds to it
			Result<Type> variableType = goalManager
					.registerSubgoal(new BoundTypeGoal(localVariable));
			inferredType.add(variableType);
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
			if (inferredType.isFinished())
				return;

			addUnqualifiedBindingsBelowCodeObject(nestedCodeObject);
		}
	}
}
