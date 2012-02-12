package uk.ac.ic.doc.gander.importing;

import java.util.Collections;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;

final class FromImportAsBindingScheme<O, C, M> implements BindingScheme<M> {

	private final Import<O, C, M> importInstance;
	private final ImportSimulator.Binder<O, C, M> bindingHandler;
	private final Loader<O, M> loader;

	public FromImportAsBindingScheme(Import<O, C, M> importInstance,
			Binder<O, C, M> bindingHandler, Loader<O, M> loader) {
		assert importInstance != null;
		assert bindingHandler != null;
		assert loader != null;

		this.importInstance = importInstance;
		this.bindingHandler = bindingHandler;
		this.loader = loader;
	}

	@Override
	public void bindItems(M previouslyLoadedModule) {
		if (previouslyLoadedModule == null) {
			throw new NullPointerException(
					"Must have a module to import items with respect to");
		}

		String name = importInstance.specification().boundObjectName();

		M submodule = loader.loadModule(Collections.singletonList(name),
				previouslyLoadedModule);

		/*
		 * Resolve item name to an item relative the loaded module. If the item
		 * is a module it will have been loaded and passed to us here. Otherwise
		 * we try and find an item answering to that name.
		 */
		if (submodule == null) {
			O object = loader.loadModuleMember(name, previouslyLoadedModule);

			if (object != null) {
				bindingHandler.bindObjectToLocalName(object, importInstance
						.specification().bindingName(), importInstance
						.container());
			} else {
				// TODO: distinguish the object case
				bindingHandler.onUnresolvedImport(importInstance, name,
						previouslyLoadedModule);
			}
		} else {
			bindingHandler.bindModuleToLocalName(submodule, importInstance
					.specification().bindingName(), importInstance.container());
		}
	}

	@Override
	public BindingBehaviour modulePathBindingBehaviour() {
		return FromImportBindingBehaviour.INSTANCE;
	}
}