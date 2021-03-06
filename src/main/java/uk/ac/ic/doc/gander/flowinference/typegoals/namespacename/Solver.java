package uk.ac.ic.doc.gander.flowinference.typegoals.namespacename;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.Namespace;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyModule;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyUnresolvedImport;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Processor;
import uk.ac.ic.doc.gander.flowinference.typegoals.expression.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.importing.Import;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.WholeModelImportSimulation;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.ModuleNamespace;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

final class NamespaceNameTypeGoalSolver {

    private final NamespaceName name;
    private final SubgoalManager goalManager;

    private final RedundancyEliminator<PyObject> completeType = new RedundancyEliminator<PyObject>();

    NamespaceNameTypeGoalSolver(NamespaceName name, SubgoalManager goalManager) {
        if (name == null)
            throw new NullPointerException(
                    "Can't find an name's type if we don't have a name");
        if (goalManager == null)
            throw new NullPointerException(
                    "We need to be able to issue subqueries");

        this.name = name;
        this.goalManager = goalManager;

        addTypesFromNamespace();

        if (name.namespace() instanceof Class) {
            // XXX: what about inherited objects? Their namespace is not class
            addTypesFromInheritanceChain((Class) name.namespace());
        }

        if (name.namespace() instanceof ModuleNamespace) {
            addTypesFromIntermediateModuleImport((ModuleNamespace) name
                    .namespace());
        }
    }

    Result<PyObject> solution() {
        return completeType.result();
    }

    private void addTypesFromNamespace() {

        completeType.add(new UnqualifiedNameDefinitionsPartialSolution(
                goalManager, name).partialSolution());

        if (!completeType.isFinished()) {
            completeType.add(new QualifiedNameDefinitionsPartialSolution(
                    goalManager, name).partialSolution());
        }
    }

    private void addTypesFromInheritanceChain(Class klass) {

        for (exprType supertype : klass.inheritsFrom()) {

            /*
             * Methods in a subclass override those declared in a superclass but
             * we must flow both into the subclass's namespace as the subclass's
             * version could be deleted leading to calls invoking the superclass
             * version.
             */

            Result<PyObject> supertypeTypes = goalManager
                    .registerSubgoal(new ExpressionTypeGoal(
                            new ModelSite<exprType>(supertype, klass
                                    .codeObject().parent())));

            MemberTyper memberTyper = new MemberTyper();
            supertypeTypes.actOnResult(memberTyper);
        }
    }

    /**
     * Modules can be very sneakily bound to a token in another module by an
     * import statement in a third module. This method catches those kinds of
     * import.
     * 
     * @param namespace
     *            the target namespace that might get a name bound to a module
     *            type in this sneaky way
     */
    private void addTypesFromIntermediateModuleImport(
            final ModuleNamespace namespace) {
        if (completeType.isFinished())
            return;

        final Set<PyObject> type = new HashSet<PyObject>();

        new WholeModelImportSimulation(namespace.model(),
                new Binder<NamespaceName, Namespace, CodeObject, ModuleCO>() {

                    @Override
                    public void bindModuleToLocalName(ModuleCO loadedModule,
                            String name, CodeObject container) {
                        // Handled by UnqualifiedNameDefinitionsPartialSolution
                    }

                    @Override
                    public void bindObjectToLocalName(
                            NamespaceName importedObject, String name,
                            CodeObject container) {
                        // Handled by UnqualifiedNameDefinitionsPartialSolution
                    }

                    @Override
                    public void bindModuleToName(ModuleCO loadedModule,
                            String importName, ModuleCO receivingModule) {

                        NamespaceName bindingName = new NamespaceName(
                                new Variable(importName, receivingModule)
                                        .bindingLocation());

                        if (bindingName.equals(name)) {
                            type.add(new PyModule(loadedModule));
                        }
                    }

                    @Override
                    public void bindObjectToName(NamespaceName importedObject,
                            String name, ModuleCO receivingModule) {
                        /*
                         * It's not possible for an object to be the
                         * intermediate segment of an import statement
                         */
                    }

                    @Override
                    public void bindAllNamespaceMembers(Namespace allMembers,
                            CodeObject container) {
                        /*
                         * It's not possible for an object to be the
                         * intermediate segment of an import statement
                         */
                    }

                    @Override
                    public void onUnresolvedImport(
                            Import<CodeObject, ModuleCO> importInstance,
                            String importName, ModuleCO receivingModule) {

                        NamespaceName bindingName = new NamespaceName(
                                new Variable(importName, receivingModule)
                                        .bindingLocation());

                        if (bindingName.equals(name)) {
                            type.add(new PyUnresolvedImport(importInstance));
                        }
                    }

                    @Override
                    public void onUnresolvedLocalImport(
                            Import<CodeObject, ModuleCO> importInstance,
                            String name) {

                        // Handled by UnqualifiedNameDefinitionsPartialSolution
                    }
                });

        completeType.add(new FiniteResult<PyObject>(type));
    }

    private final class MemberTyper implements Processor<PyObject> {

        @Override
        public void processInfiniteResult() {
            // do nothing, no member found
        }

        @Override
        public void processFiniteResult(Set<PyObject> result) {
            for (PyObject supertypeType : result) {
                if (completeType.isFinished())
                    break;

                completeType.add(supertypeType.memberType(name.name(),
                        goalManager));
            }
        }
    }
}