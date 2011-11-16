package uk.ac.ic.doc.gander.importing;

import java.util.List;

import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;

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

	private final ImportSimulationWatcher callback;
	private final Model model;

	public WholeModelImportSimulation(Model model,
			ImportSimulationWatcher callback) {
		this.model = model;
		this.callback = callback;
		walkModel();
	}

	void walkModel() {

		new WholeModelImportVisitation(model, new ImportHandler() {

			public void onImport(Namespace importReceiver, String moduleName) {
				newImportSimulator(importReceiver).simulateImport(moduleName);
			}

			public void onImportAs(Namespace importReceiver, String moduleName,
					String asName) {
				newImportSimulator(importReceiver).simulateImportAs(moduleName,
						asName);
			}

			public void onImportFrom(Namespace importReceiver,
					String moduleName, String itemName) {
				newImportSimulator(importReceiver).simulateImportFrom(
						moduleName, itemName);
			}

			public void onImportFromAs(Namespace importReceiver,
					String moduleName, String itemName, String asName) {
				newImportSimulator(importReceiver).simulateImportFromAs(
						moduleName, itemName, asName);
			}

		});
	}

	private ImportSimulator newImportSimulator(Namespace importReceiver) {
		return new ImportSimulator(importReceiver) {

			@Override
			protected Module simulateLoad(List<String> importPath,
					Module relativeToPackage) {
				return relativeToPackage.lookup(importPath);
			}

			@Override
			protected Module simulateLoad(List<String> importPath) {
				return model.lookup(importPath);
			}

			@Override
			protected void onUnresolvedImportFromItem(List<String> fromPath,
					String itemName, Module relativeToPackage,
					Namespace importReceiver, String as) {
				// TODO Auto-generated method stub

			}

			@Override
			protected void onUnresolvedImport(List<String> importPath,
					Module relativeToPackage, Namespace importReceiver,
					String as) {
				// TODO Auto-generated method stub

			}

			@Override
			protected void bindName(Namespace importReceiver,
					Namespace loadedObject, String as) {
				callback.bindingName(importReceiver, loadedObject, as);
			}
		};
	}
}
