package uk.ac.ic.doc.gander.flowinference.flowgoals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.importing.ImportSimulationWatcher;
import uk.ac.ic.doc.gander.importing.WholeModelImportSimulation;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.name_binding.Binder;
import uk.ac.ic.doc.gander.model.name_binding.ScopedVariable;

/**
 * Flow goal for a function, class or module's value flow in a single execution.
 * 
 * A function or class definition creates an object that flows in a single step
 * to:
 * <ul>
 * <li>the namespace of the code block in which it is defined as the name with
 * which it is declared</li>
 * <li>the namespace of any code block containing {@code from l import X} with
 * the name {@code X}</li>
 * </ul>
 * 
 * A module's flows is more complicated. A module {@code m} flows in a single
 * step to (assuming always that {@code m} resolves to the module in question
 * taking relative paths into account):
 * <ul>
 * <li>the namespace of any code block containing {@code import m} with the name
 * {@code m}</li>
 * <li>the namespace of any code block containing {@code import m as p} with the
 * name {@code p}</li>
 * <li>the namespace of any module that appears before {@code m} in a string
 * such as {@code import k.l.m}, in this case {@code l}, with the name {@code m}
 * </li>
 * <li>the namespace of any module that appears before {@code m} in a string
 * such as {@code import k.l.m as p}, in this case {@code l}, with the name
 * {@code m} and into the namespace of the code block containing the import
 * statement with the name {@code p}</li>
 * <li>the namespace of any code block containing {@code from l import m} with
 * the name {@code m}</li>
 * </ul>
 */
final class CodeObjectStepGoal implements FlowStepGoal {

	private final Namespace codeObject;

	CodeObjectStepGoal(Namespace codeObject) {
		this.codeObject = codeObject;
	}

	public Set<FlowPosition> initialSolution() {
		return Collections.emptySet();
	}

	public Set<FlowPosition> recalculateSolution(SubgoalManager goalManager) {

		Set<FlowPosition> positions = new HashSet<FlowPosition>();

		addLocalFlow(positions);
		//addNamespacesNamesThatReferenceOurCodeObject(positions);

		return positions;
	}

	/**
	 * Add positions for the flow of code object's value that occur simply
	 * through its declaration.
	 * 
	 * A code object declaration flows the newly created code object subject to
	 * the following rules:
	 * 
	 * A function declared in a module flows into the module's namespace.
	 * 
	 * A class declared in a module flows into the module's namespace.
	 * 
	 * A function declared in a class flows into the class's namespace unless
	 * the name of the nested function was declared global in the outer class in
	 * which case it flows to the global namespace.
	 * 
	 * A class declared in a class flow into the class's namespace unless the
	 * name of the nested class was declared global in the outer class in which
	 * case it flows to the global namespace.
	 * 
	 * A function declared in a function flows into the function's namespace
	 * unless the name of the nested function was declared global in the outer
	 * function in which case it flows to the global namespace.
	 * 
	 * A class declared in a function flows into the function's namespace unless
	 * the name of the nested function was declared global in the outer function
	 * in which case it flows to the global namespace.
	 * 
	 * A module doesn't flow anywhere by default. It has to be imported. That
	 * flow is handled in {@link addNamespacesNamesThatReferenceOurModule}.
	 */
	private void addLocalFlow(Set<FlowPosition> positions) {

		/* Modules don't flow anywhere by default */
		if (codeObject instanceof Module) {
			return;
		}

		/*
		 * First we have to find what scope the code object's name binds in when
		 * appearing in the code object's parent. This will either be the
		 * parent's namespace or the global namespace. No other namespace are
		 * possible when binding a name (though others are possible when just
		 * reading the name).
		 */
		ScopedVariable nameBinding = Binder.resolveBindingScope(codeObject.getName(),
				codeObject.getParentScope());
		positions.add(new NamespaceKeyPosition(nameBinding));

	}

	private void addNamespacesNamesThatReferenceOurCodeObject(
			final Set<FlowPosition> positions) {

		/*
		 * Our code object could be imported anywhere in the system so we have
		 * to walk the entire thing searching any code block for import
		 * statements that result in our code object being imported somewhere.
		 * 
		 * That somewhere is not necessarily the namespace of the code object
		 * containing the import statement. In the case of dotted import names
		 * it will be the namespace of the segment of the dotted name to the
		 * left, for instance.
		 */
		new WholeModelImportSimulation(codeObject.model(), new ImportSimulationWatcher() {

			public void bindingName(Namespace importReceiver,
					Namespace loadedObject, String as) {

				/*
				 * importReceiver may not actually be the import receiver. It
				 * depends on the binding scope of 'as' in importReceiver. It
				 * could be the global scope so we resolve the name here.
				 */

				ScopedVariable nameBinding = Binder.resolveBindingScope(as,
						importReceiver);
				assert nameBinding.bindingLocation().namespace().equals(importReceiver)
						|| nameBinding.bindingLocation().namespace().equals(
								importReceiver.getGlobalNamespace());

				if (loadedObject.equals(codeObject)) {
					positions.add(new NamespaceKeyPosition(nameBinding));
				}
			}
		});
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
		CodeObjectStepGoal other = (CodeObjectStepGoal) obj;
		if (codeObject == null) {
			if (other.codeObject != null)
				return false;
		} else if (!codeObject.equals(other.codeObject))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CodeObjectStepGoal [codeObject=" + codeObject + "]";
	}

}
