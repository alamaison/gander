package uk.ac.ic.doc.gander.flowinference.typegoals.namespacename;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.NamespaceName;
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

	@Override
	public Result<Type> partialSolution() {
		return inferredType.result();
	}

	UnqualifiedNameDefinitionsPartialSolution(SubgoalManager goalManager,
			NamespaceName name) {
		assert goalManager != null;
		assert name != null;

		for (Variable variable : name.namespace().variablesWriteableInScope(
				name.name())) {

			/*
			 * We're sure that the name in this code object is talking about the
			 * same namespace location that we are interested in. So now we want
			 * to know what this code object binds to it
			 */
			/*
			 * FIXME: Assertion doesn't work because variablesWriteableInScope
			 * may return variables in the builtin module. It shouldn't.
			 */
			// assert new
			// NamespaceName(variable.bindingLocation()).equals(name);

			VariableTypeSummariser df = new VariableTypeSummariser(variable,
					goalManager);
			inferredType.add(df.solution());

			if (inferredType.isFinished())
				return;
		}
	}

}
