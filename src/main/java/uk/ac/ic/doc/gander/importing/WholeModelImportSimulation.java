package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.flowinference.Namespace;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.NamespaceNameLoader;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;

/**
 * Simulation of Python's elaboration phase with respect to importing.
 * 
 * Roughly speaking, this simulation models what happens when a Python program
 * starts up. This is not strictly correct as some imports happen later, for
 * instance imports inside a function code block only occur when the function is
 * executed. This simulation pretends all imports occur immediately.
 * 
 * XXX: Why does it depend on all imported modules already being loaded?
 */
public final class WholeModelImportSimulation {

    private final Binder<NamespaceName, Namespace, CodeObject, ModuleCO> callback;
    private final Model model;

    public WholeModelImportSimulation(Model model,
            Binder<NamespaceName, Namespace, CodeObject, ModuleCO> callback) {
        this.model = model;
        this.callback = callback;
        walkModel();
    }

    private void walkModel() {

        ImportHandler<CodeObject, ModuleCO> handler = new ImportHandler<CodeObject, ModuleCO>() {

            @Override
            public void onImport(Import<CodeObject, ModuleCO> importInstance) {
                newImportSimulator().simulateImport(importInstance);
            }

        };

        new WholeModelImportVisitation(model, handler);

    }

    private ImportSimulator<NamespaceName, Namespace, CodeObject, ModuleCO> newImportSimulator() {
        return ImportSimulator.newInstance(callback, new NamespaceNameLoader(
                model));
    }
}
