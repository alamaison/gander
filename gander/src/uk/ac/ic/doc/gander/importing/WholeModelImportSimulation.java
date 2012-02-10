package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.NamespaceNameLoader;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;

/**
 * Simulation of Python's elaboration phase with respect to importing.
 * 
 * Roughly speaking, this simulation models what happens when a Python program
 * starts up. This is not strictly correct as some imports happen later, for
 * instance imports inside a function code block only occur when the function is
 * executed. This simulation pretends all imports occur immediately.
 * 
 * XXX: Why does it depend on all imported modules already being loaded?
 */
public final class WholeModelImportSimulation {

	private final Binder<NamespaceName, CodeObject, ModuleCO> callback;
	private final Model model;

	public WholeModelImportSimulation(Model model,
			Binder<NamespaceName, CodeObject, ModuleCO> callback) {
		this.model = model;
		this.callback = callback;
		walkModel();
	}

	private void walkModel() {

		new WholeModelImportVisitation(model, new ImportHandler<CodeObject>() {

			@Override
			public void onImport(CodeObject importReceiver,
					StaticImportSpecification importStatement) {

				ModuleCO relativeTo = null;
				Module relativeToPackage = importReceiver.enclosingModule()
						.oldStyleConflatedNamespace().getParent();
				if (relativeToPackage != null) {
					relativeTo = relativeToPackage.codeObject();
				}

				Import<NamespaceName, CodeObject, ModuleCO> importInstance = ImportFactory
						.newImport(importStatement, relativeTo, importReceiver);
				newImportSimulator().simulateImport(importInstance);
			}

		});

	}

	private ImportSimulator<NamespaceName, CodeObject, ModuleCO> newImportSimulator() {
		return new ImportSimulator<NamespaceName, CodeObject, ModuleCO>(
				callback, new NamespaceNameLoader(model));
	}
}
