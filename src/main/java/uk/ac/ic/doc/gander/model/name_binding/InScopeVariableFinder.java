package uk.ac.ic.doc.gander.model.name_binding;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.gander.model.CodeObjectWalker;
import uk.ac.ic.doc.gander.model.ModelWalker;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

/**
 * Finds variables that are in the scope of the given code object with the given
 * name.
 */
public final class InScopeVariableFinder {

    private final Set<Variable> variables = new HashSet<Variable>();
    private final NamespaceName namespaceName;
    private final boolean doingABuiltin;

    /**
     * XXX: This is a mess. We should take a NamespaceName but then how do we
     * get at the code objects to search for variable.
     */
    public InScopeVariableFinder(CodeObject scope, String nameToLookFor) {

        this.namespaceName = new NamespaceName(nameToLookFor,
                scope.unqualifiedNamespace());

        if (scope.isBuiltin() && scope.equals(scope.enclosingModule())) {

            /* The builtin module scope can be anywhere in the model */
            doingABuiltin = true;
            doBuiltinScopeSearch();
        } else {
            /*
             * Non-builtin modules and all non-modules can only have scope in
             * their nested code objects.
             */
            doingABuiltin = false;
            addVariableIfInScope(scope);
        }
    }

    public Set<Variable> variables() {
        return variables;
    }

    /**
     * Search code object for variables that would bind in the given namespace
     * name.
     * 
     * Recurses into nested code objects.
     * 
     * @param codeObject
     *            the code object to search
     */
    private void addVariableIfInScope(CodeObject codeObject) {

        Variable localVariable = new Variable(namespaceName.name(), codeObject);
        BindingLocation bindingLocation = localVariable.bindingLocation();
        NamespaceName bindingNamespaceName = new NamespaceName(bindingLocation);

        if (bindingNamespaceName.equals(namespaceName)
                || (doingABuiltin && bindingNamespaceName.namespace().equals(
                        codeObject.enclosingModule().unqualifiedNamespace()))) {
            variables.add(localVariable);
        }

        for (CodeObject nestedCodeObject : codeObject.nestedCodeObjects()) {
            addVariableIfInScope(nestedCodeObject);
        }
    }

    private void doBuiltinScopeSearch() {

        new ModelWalker() {

            @Override
            protected void visitModule(Module module) {
                new CodeObjectWalker() {

                    @Override
                    protected void visitCodeObject(CodeObject codeObject) {
                        addVariableIfInScope(codeObject);
                    }
                }.walk(module.codeObject());
            }
        }.walk(namespaceName.namespace().model());

    }
}