package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.LegacyImportSimulator.Binder;
import uk.ac.ic.doc.gander.model.LegacyModelLookupLoader;
import uk.ac.ic.doc.gander.model.Model;
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

	private final Binder<CodeObject, CodeObject, ModuleCO> callback;
	private final Model model;

	public WholeModelImportSimulation(Model model,
			Binder<CodeObject, CodeObject, ModuleCO> callback) {
		this.model = model;
		this.callback = callback;
		walkModel();
	}

	private void walkModel() {

		new WholeModelImportVisitation(model, new ImportHandler<CodeObject>() {

			public void onImport(CodeObject importReceiver, String moduleName) {
				newImportSimulator(importReceiver).simulateImport(moduleName);
			}

			public void onImportAs(CodeObject importReceiver,
					String moduleName, String asName) {
				newImportSimulator(importReceiver).simulateImportAs(moduleName,
						asName);
			}

			public void onImportFrom(CodeObject importReceiver,
					String moduleName, String itemName) {
				newImportSimulator(importReceiver).simulateImportFrom(
						moduleName, itemName);
			}

			public void onImportFromAs(CodeObject importReceiver,
					String moduleName, String itemName, String asName) {
				newImportSimulator(importReceiver).simulateImportFromAs(
						moduleName, itemName, asName);
			}

		});
	}

	private LegacyImportSimulator<CodeObject, CodeObject, ModuleCO> newImportSimulator(
			CodeObject importReceiver) {
		return new LegacyImportSimulator<CodeObject, CodeObject, ModuleCO>(
				importReceiver, callback, new LegacyModelLookupLoader(model));
	}
}
