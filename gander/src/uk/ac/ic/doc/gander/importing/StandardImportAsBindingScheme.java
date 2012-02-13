package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;

final class StandardImportAsBindingScheme<M> implements BindingScheme<M> {

	public static <O, C, M> StandardImportAsBindingScheme<M> newInstance() {
		return new StandardImportAsBindingScheme<M>();
	}

	private StandardImportAsBindingScheme() {
		// just here to make constructor private
	}

	@Override
	public BindingBehaviour modulePathBindingBehaviour() {
		return StandardImportAsBindingBehaviour.INSTANCE;
	}

	@Override
	public ItemBindingStage<M> itemBinding(M sourceModule) {
		return new ItemBindingStage<M>() {

			@Override
			public <O, A, C> void doBinding(Import<O, C, M> importInstance,
					Binder<O, A, C, M> bindingHandler, Loader<O, A, M> loader) {
			}
		};
	}

}