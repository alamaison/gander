package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.model.Model;
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

			public void onImport(CodeObject importReceiver, String moduleName) {

				ModuleCO relativeTo = null;
				if (importReceiver.enclosingModule()
						.oldStyleConflatedNamespace().getParent() != null) {
					relativeTo = importReceiver.enclosingModule()
							.oldStyleConflatedNamespace().getParent()
							.codeObject();
				}

				Import<NamespaceName, CodeObject, ModuleCO> importInstance = ImportFactory
						.newImport(moduleName, relativeTo, importReceiver);
				newImportSimulator().simulateImport(importInstance);
			}

			public void onImportAs(CodeObject importReceiver,
					String moduleName, String asName) {

				ModuleCO relativeTo = null;
				if (importReceiver.enclosingModule()
						.oldStyleConflatedNamespace().getParent() != null) {
					relativeTo = importReceiver.enclosingModule()
							.oldStyleConflatedNamespace().getParent()
							.codeObject();
				}

				Import<NamespaceName, CodeObject, ModuleCO> importInstance = ImportFactory
						.newImportAs(moduleName, asName, relativeTo,
								importReceiver);

				newImportSimulator().simulateImport(importInstance);
			}

			public void onImportFrom(CodeObject importReceiver,
					String moduleName, String itemName) {

				ModuleCO relativeTo = null;
				if (importReceiver.enclosingModule()
						.oldStyleConflatedNamespace().getParent() != null) {
					relativeTo = importReceiver.enclosingModule()
							.oldStyleConflatedNamespace().getParent()
							.codeObject();
				}

				Import<NamespaceName, CodeObject, ModuleCO> importInstance = ImportFactory
						.newFromImport(moduleName, itemName, relativeTo,
								importReceiver);
				newImportSimulator().simulateImport(importInstance);
			}

			public void onImportFromAs(CodeObject importReceiver,
					String moduleName, String itemName, String asName) {

				ModuleCO relativeTo = null;
				if (importReceiver.enclosingModule()
						.oldStyleConflatedNamespace().getParent() != null) {
					relativeTo = importReceiver.enclosingModule()
							.oldStyleConflatedNamespace().getParent()
							.codeObject();
				}

				Import<NamespaceName, CodeObject, ModuleCO> importInstance = ImportFactory
						.newFromImportAs(moduleName, itemName, asName,
								relativeTo, importReceiver);
				newImportSimulator().simulateImport(importInstance);
			}

		});

	}

	private ImportSimulator<NamespaceName, CodeObject, ModuleCO> newImportSimulator() {
		return new ImportSimulator<NamespaceName, CodeObject, ModuleCO>(
				callback, new NamespaceNameLoader(model));
	}
}
