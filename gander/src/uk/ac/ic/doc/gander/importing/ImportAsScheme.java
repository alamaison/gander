package uk.ac.ic.doc.gander.importing;

final class ImportAsScheme<O, C, M> implements BindingScheme<M, M> {

	private final C outerImportReceiver;
	private final String asName;
	private final ImportSimulator.Binder<O, C, M> bindingHandler;

	ImportAsScheme(C outerImportReceiver, String asName,
			ImportSimulator.Binder<O, C, M> bindingHandler) {
		assert outerImportReceiver != null;
		assert !asName.isEmpty();
		assert bindingHandler != null;
		this.outerImportReceiver = outerImportReceiver;
		this.asName = asName;
		this.bindingHandler = bindingHandler;
	}

	public void bindSolitaryToken(M module, String name) {
		bindingHandler.bindModuleToLocalName(module, asName,
				outerImportReceiver);
	}

	public void bindFirstToken(M module, String name) {
	}

	public void bindIntermediateToken(M module, String name,
			M previouslyLoadedModule) {
		bindingHandler.bindModuleToName(module, name, previouslyLoadedModule);
	}

	public void bindFinalToken(M module, String name, M previouslyLoadedModule) {
		bindingHandler.bindModuleToName(module, name, previouslyLoadedModule);
		bindingHandler.bindModuleToLocalName(module, asName,
				outerImportReceiver);
	}

}