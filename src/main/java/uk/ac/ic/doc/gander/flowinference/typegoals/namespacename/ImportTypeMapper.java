package uk.ac.ic.doc.gander.flowinference.typegoals.namespacename;

import java.util.Collections;

import uk.ac.ic.doc.gander.flowinference.Namespace;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyModule;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyUnresolvedImport;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.importing.Import;
import uk.ac.ic.doc.gander.importing.ImportSimulator;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.NamespaceNameLoader;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

final class ImportTypeMapper {

    private final SubgoalManager goalManager;

    ImportTypeMapper(SubgoalManager goalManager) {
        this.goalManager = goalManager;
    }

    Result<PyObject> typeImport(Variable variable,
            Import<CodeObject, ModuleCO> importInstance) {
        LocalTypeBinder typingBinder = new LocalTypeBinder(variable);

        ImportSimulator<NamespaceName, Namespace, CodeObject, ModuleCO> simulator = ImportSimulator
                .newInstance(typingBinder, new NamespaceNameLoader(variable
                        .codeObject().model()));
        simulator.simulateImport(importInstance);

        return typingBinder.partialVariableType.result();
    }

    private final class LocalTypeBinder implements
            Binder<NamespaceName, Namespace, CodeObject, ModuleCO> {

        private final Variable variable;
        private final RedundancyEliminator<PyObject> partialVariableType = new RedundancyEliminator<PyObject>();

        public LocalTypeBinder(Variable variable) {
            this.variable = variable;
        }

        @Override
        public void bindModuleToLocalName(ModuleCO loadedModule, String name,
                CodeObject container) {
            // FIXME: triggers incorrectly due to nasty 'top-level' module
            // assert container.equals(variable.codeObject()) : "Container: "
            // + container + " Variable: " + variable;

            if (!partialVariableType.isFinished()
                    && name.equals(variable.name())) {

                partialVariableType.add(new FiniteResult<PyObject>(Collections
                        .singleton(new PyModule(loadedModule))));
            }
        }

        @Override
        public void bindObjectToLocalName(NamespaceName importedObject,
                String name, CodeObject container) {
            // FIXME: triggers incorrectly due to nasty 'top-level' module
            // assert container.equals(variable.codeObject()) : "Container: "
            // + container + " Variable: " + variable;

            if (!partialVariableType.isFinished()
                    && name.equals(variable.name())) {

                partialVariableType.add(goalManager
                        .registerSubgoal(new NamespaceNameTypeGoal(
                                importedObject)));
            }
        }

        @Override
        public void bindModuleToName(ModuleCO loadedModule, String name,
                ModuleCO receivingModule) {
            // assert !receivingModule.equals(variable.codeObject());
        }

        @Override
        public void bindObjectToName(NamespaceName importedObject, String name,
                ModuleCO receivingModule) {
            assert !receivingModule.equals(variable.codeObject()) : "Receiving module: "
                    + receivingModule + " Variable: " + variable;
        }

        @Override
        public void bindAllNamespaceMembers(Namespace allMembers,
                CodeObject container) {
            assert container.equals(variable.codeObject());

            /*
             * event though all names are imported, we are interested in a
             * specific name so can issue a query for it
             */
            if (!partialVariableType.isFinished()) {
                partialVariableType
                        .add(goalManager
                                .registerSubgoal(new NamespaceNameTypeGoal(
                                        new NamespaceName(variable.name(),
                                                allMembers))));
            }
        }

        @Override
        public void onUnresolvedImport(
                Import<CodeObject, ModuleCO> importInstance, String name,
                ModuleCO receivingModule) {
            // assert !receivingModule.equals(variable.codeObject()) :
            // "Receiving module: "
            // + receivingModule + " Variable: " + variable;
        }

        @Override
        public void onUnresolvedLocalImport(
                Import<CodeObject, ModuleCO> importInstance, String name) {

            if (name.equals(variable.name())) {
                partialVariableType.add(new FiniteResult<PyObject>(Collections
                        .singleton(new PyUnresolvedImport(importInstance))));
            }
        }
    }
}