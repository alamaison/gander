package uk.ac.ic.doc.gander.flowinference.flowgoals;

import java.util.Collections;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.NamedCodeObject;
import uk.ac.ic.doc.gander.model.codeobject.NestedCodeObject;
import uk.ac.ic.doc.gander.model.name_binding.BindingLocation;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

/**
 * Model of the single-step value flow that occurs simply by the appearance of
 * code object's declaration.
 * 
 * A code object declaration flows the newly created code object onwards subject
 * to the following rules:
 * 
 * A function declared in a module flows into the module's namespace.
 * 
 * A class declared in a module flows into the module's namespace.
 * 
 * A function declared in a class flows into the class's namespace unless the
 * name of the nested function was declared global in the outer class in which
 * case it flows to the global namespace.
 * 
 * A class declared in a class flow into the class's namespace unless the name
 * of the nested class was declared global in the outer class in which case it
 * flows to the global namespace.
 * 
 * A function declared in a function flows into the function's namespace unless
 * the name of the nested function was declared global in the outer function in
 * which case it flows to the global namespace.
 * 
 * A class declared in a function flows into the function's namespace unless the
 * name of the nested function was declared global in the outer function in
 * which case it flows to the global namespace.
 * 
 * A module doesn't flow anywhere by default. It has to be imported.
 */
final class CodeObjectDefinitionFlowStepGoal implements FlowStepGoal {

	private final CodeObject codeObject;

	CodeObjectDefinitionFlowStepGoal(CodeObject codeObject) {
		this.codeObject = codeObject;
	}

	public Result<FlowPosition> initialSolution() {
		return FiniteResult.bottom();
	}

	public Result<FlowPosition> recalculateSolution(SubgoalManager goalManager) {

		/*
		 * Only code object contained within another can flow their code object
		 * into a namespace simply by existing. In other words, modules don't
		 * flow anywhere by default but everything else does.
		 */
		if (codeObject instanceof NestedCodeObject) {

			/*
			 * First we have to find what scope the code object's name binds in
			 * when appearing in the code object's parent. This will either be
			 * the parent's namespace or the global namespace. No other
			 * namespace are possible when binding a name (though others are
			 * possible when just reading the name).
			 */
			if (codeObject instanceof NamedCodeObject) {
				/*
				 * We rely on the declared name of our code object to bind it to
				 * the same name in the namespace lexically in scope for that
				 * name
				 */
				Variable nameBinding = new Variable(
						((NamedCodeObject) codeObject).declaredName(),
						((NestedCodeObject) codeObject).parent());

				BindingLocation bindingLocation = nameBinding.bindingLocation();
				NamespaceName namespaceName = new NamespaceName(bindingLocation);

				return new FiniteResult<FlowPosition>(Collections
						.singleton(new NamespaceNamePosition(namespaceName)));
			} else {
				/*
				 * TODO: handle lambdas etc which are unnamed code object. These
				 * will probably have to try to detect their own flow situation
				 * as they can't just flow to a name in the local namespace.
				 */
			}
		}

		return FiniteResult.bottom();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((codeObject == null) ? 0 : codeObject.hashCode());
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
		CodeObjectDefinitionFlowStepGoal other = (CodeObjectDefinitionFlowStepGoal) obj;
		if (codeObject == null) {
			if (other.codeObject != null)
				return false;
		} else if (!codeObject.equals(other.codeObject))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CodeObjectDefinitionFlowStepGoal [codeObject=" + codeObject
				+ "]";
	}

}
