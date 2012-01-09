package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;

final class FromImportAsScheme<O, C, M> implements ModuleBindingScheme<M> {

	private final C outerImportReceiver;
	private final String asName;
	private final ImportSimulator.Binder<O, C, M> bindingHandler;
	private final Loader<O, C, M> loader;

	FromImportAsScheme(C outerImportReceiver, String asName,
			ImportSimulator.Binder<O, C, M> bindingHandler,
			ImportSimulator.Loader<O, C, M> loader) {
		assert outerImportReceiver != null;
		assert !asName.isEmpty();
		assert bindingHandler != null;
		assert loader != null;

		this.outerImportReceiver = outerImportReceiver;
		this.asName = asName;
		this.bindingHandler = bindingHandler;
		this.loader = loader;
	}

	public void bindSolitaryToken(M module, String name) {
		bindFirstToken(module, name);
	}

	public void bindFirstToken(M object, String name) {
	}

	public void bindIntermediateToken(M object, String name,
			M previouslyLoadedModule) {
		bindingHandler.bindModuleToName(object, name, previouslyLoadedModule);
	}

	public void bindFinalToken(M module, String name, M previouslyLoadedModule) {
		/*
		 * Resolve item name to an item relative the loaded module. If the item
		 * is a module it will have been loaded and passed to us here. Otherwise
		 * we try and find an item answering to that name.
		 */
		if (module == null) {
			O object = loader.loadNonModuleMember(name, previouslyLoadedModule);
			bindingHandler.bindObjectToLocalName(object, asName,
					outerImportReceiver);
		} else {
			bindingHandler.bindModuleToLocalName(module, asName,
					outerImportReceiver);
		}
	}

}