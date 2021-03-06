package uk.ac.ic.doc.gander.importing;

import java.util.Collections;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;

enum FromImportAsBindingScheme implements BindingScheme {

    INSTANCE;

    @Override
    public BindingBehaviour modulePathBindingBehaviour() {
        return FromImportBindingBehaviour.INSTANCE;
    }

    @Override
    public ItemBindingStage itemBinding() {

        return new ItemBindingStage() {

            @Override
            public <O, A, C, M> void doBinding(Import<C, M> importInstance,
                    Binder<O, A, C, M> bindingHandler, Loader<O, A, M> loader,
                    M sourceModule) {

                if (sourceModule == null) {
                    throw new NullPointerException(
                            "Must have a module to import items with respect to");
                }

                StaticImportStatement specification = (StaticImportStatement) importInstance
                        .statement();
                C container = importInstance.container();
                String name = specification.boundObjectName();

                M submodule = loader.loadModule(
                        Collections.singletonList(name), sourceModule);

                /*
                 * Resolve item name to an item relative the loaded module. If
                 * the item is a module it will have been loaded and passed to
                 * us here. Otherwise we try and find an item answering to that
                 * name.
                 */
                if (submodule == null) {
                    O object = loader.loadModuleNamespaceMember(name,
                            sourceModule);

                    if (object != null) {
                        bindingHandler.bindObjectToLocalName(object,
                                specification.bindingName(), container);
                    } else {
                        // TODO: distinguish the object case
                        bindingHandler.onUnresolvedImport(importInstance, name,
                                sourceModule);
                    }
                } else {
                    bindingHandler.bindModuleToLocalName(submodule,
                            specification.bindingName(), container);
                }
            }
        };
    }
}