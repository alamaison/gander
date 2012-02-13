package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;

interface BindingScheme<M> {

	BindingBehaviour modulePathBindingBehaviour();
	
	interface ItemBindingStage<M> {
		
		 <O, A, C> void doBinding(Import<O, C, M> importInstance,
			Binder<O, A, C, M> bindingHandler, Loader<O, A, M> loader);

	}
	
	ItemBindingStage<M> itemBinding(M sourceModule);
}