package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;

interface BindingScheme {

	BindingBehaviour modulePathBindingBehaviour();

	interface ItemBindingStage {

		<O, A, C, M> void doBinding(Import<C, M> importInstance,
				Binder<O, A, C, M> bindingHandler, Loader<O, A, M> loader,
				M sourceModule);

	}

	ItemBindingStage itemBinding();
}