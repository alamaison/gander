package uk.ac.ic.doc.gander.importing;

final class StandardImportBindingScheme<O, C, M> implements BindingScheme<M> {

	@Override
	public void bindItems(M previouslyLoadedModule) {
		// standard imports don't have items
	}

	@Override
	public BindingBehaviour modulePathBindingBehaviour() {
		return StandardImportBindingBehaviour.INSTANCE;
	}

}