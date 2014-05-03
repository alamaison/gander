package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;

enum StandardImportAsBindingScheme implements BindingScheme {

    INSTANCE;

    @Override
    public BindingBehaviour modulePathBindingBehaviour() {
        return StandardImportAsBindingBehaviour.INSTANCE;
    }

    @Override
    public ItemBindingStage itemBinding() {
        return new ItemBindingStage() {

            @Override
            public <O, A, C, M> void doBinding(Import<C, M> importInstance,
                    Binder<O, A, C, M> bindingHandler, Loader<O, A, M> loader,
                    M sourceModule) {
            }
        };
    }

}