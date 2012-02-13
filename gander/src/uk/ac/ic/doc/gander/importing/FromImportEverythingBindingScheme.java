package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;

final class FromImportEverythingBindingScheme<O, A, C, M> implements
		BindingScheme<M> {

	public static <O, A, C, M> FromImportEverythingBindingScheme<O, A, C, M> newInstance() {
		return new FromImportEverythingBindingScheme<O, A, C, M>();
	}

	private FromImportEverythingBindingScheme() {
		// just here to make constructor private
	}

	@Override
	public BindingBehaviour modulePathBindingBehaviour() {
		return FromImportBindingBehaviour.INSTANCE;
	}

	@Override
	public uk.ac.ic.doc.gander.importing.BindingScheme.ItemBindingStage<M> itemBinding(
			final M sourceModule) {
		return new ItemBindingStage<M>() {

			@Override
			public <O, A, C> void doBinding(Import<O, C, M> importInstance,
					Binder<O, A, C, M> bindingHandler, Loader<O, A, M> loader) {

				bindingHandler.bindAllNamespaceMembers(
						loader.loadAllMembersInModuleNamespace(sourceModule),
						importInstance.container());

			}
		};
	}
}