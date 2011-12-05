package uk.ac.ic.doc.gander.importing;

import java.util.List;

import uk.ac.ic.doc.gander.importing.DefaultImportSimulator.ImportEvents;
import uk.ac.ic.doc.gander.model.Member;
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

	private ImportSimulator newImportSimulator(Namespace importLocation) {
		return new DefaultImportSimulator<Member, Namespace, Module>(
				importLocation, new ImportEvents<Member, Namespace, Module>() {

					public Module loadModule(List<String> importPath,
							Module relativeToModule) {
						return relativeToModule.lookup(importPath);
					}

					public Module loadModule(List<String> importPath) {
						return model.lookup(importPath);
					}

					public void onUnresolvedImportFromItem(
							List<String> fromPath, Module relativeTo,
							String itemName, String as, Namespace codeBlock) {
						// TODO Auto-generated method stub
					}

					public void onUnresolvedImport(List<String> importPath,
							Module relativeTo, String as, Namespace codeBlock) {
						// TODO Auto-generated method stub
					}

					public void bindName(Member loadedObject, String as,
							Namespace importLocation) {
						callback.bindingName(importLocation, loadedObject, as);
					}

					public Module parentModule(Namespace importLocation) {
						if (importLocation instanceof Module)
							return ((Module) importLocation).getParent();
						else
							return importLocation.getGlobalNamespace();
					}

					public Member lookupNonModuleMember(String itemName,
							Namespace codeObjectWhoseNamespaceWeAreLoadingFrom) {
						Namespace loaded = codeObjectWhoseNamespaceWeAreLoadingFrom
								.getClasses().get(itemName);
						if (loaded == null)
							loaded = codeObjectWhoseNamespaceWeAreLoadingFrom
									.getFunctions().get(itemName);
						return loaded;
					}
				});
	}
}
