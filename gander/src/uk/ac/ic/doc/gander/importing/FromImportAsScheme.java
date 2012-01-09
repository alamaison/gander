package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;

final class FromImportAsScheme<O, C, M> implements ModuleBindingScheme<M> {

	private final M relativeTo;
	private final C outerImportReceiver;
	private final String asName;
	private final ImportSimulator.Binder<O, C, M> bindingHandler;
	private final Loader<O, C, M> loader;
	private final ImportInfo importSpec;

	public FromImportAsScheme(M relativeTo, C outerImportReceiver,
			String asName, Binder<O, C, M> bindingHandler,
			Loader<O, C, M> loader, ImportInfo importSpec) {
		assert outerImportReceiver != null;
		assert !asName.isEmpty();
		assert bindingHandler != null;
		assert loader != null;
		assert importSpec != null;

		this.relativeTo = relativeTo;
		this.outerImportReceiver = outerImportReceiver;
		this.asName = asName;
		this.bindingHandler = bindingHandler;
		this.loader = loader;
		this.importSpec = importSpec;
	}

	public void bindSolitaryToken(M module, String name) {
	}

	public void bindFirstToken(M module, String name) {
	}

	public void bindIntermediateToken(M module, String name,
			M previouslyLoadedModule) {
		if (module != null) {
			bindingHandler.bindModuleToName(module, name,
					previouslyLoadedModule);
		} else {
			bindingHandler.onUnresolvedImport(importSpec, relativeTo, name,
					previouslyLoadedModule);
		}
	}

	public void bindFinalToken(M module, String name, M previouslyLoadedModule) {
		/*
		 * Resolve item name to an item relative the loaded module. If the item
		 * is a module it will have been loaded and passed to us here. Otherwise
		 * we try and find an item answering to that name.
		 */
		if (module == null) {
			O object = loader.loadNonModuleMember(name, previouslyLoadedModule);

			if (object != null) {
				bindingHandler.bindObjectToLocalName(object, asName,
						outerImportReceiver);
			} else {
				// TODO: distinguish the object case
				bindingHandler.onUnresolvedLocalImport(importSpec, relativeTo,
						name, outerImportReceiver);
			}
		} else {
			bindingHandler.bindModuleToLocalName(module, asName,
					outerImportReceiver);
		}
	}

}