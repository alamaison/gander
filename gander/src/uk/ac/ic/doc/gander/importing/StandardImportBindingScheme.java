package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;

final class StandardImportBindingScheme<M> implements BindingScheme<M> {

	public static <M> StandardImportBindingScheme<M> newInstance() {
		return new StandardImportBindingScheme<M>();
	}

	private StandardImportBindingScheme() {
		// just here to make constructor private
	}

	@Override
	public BindingBehaviour modulePathBindingBehaviour() {
		return StandardImportBindingBehaviour.INSTANCE;
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