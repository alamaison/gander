package uk.ac.ic.doc.gander.model.name_binding;

import uk.ac.ic.doc.gander.model.LexicalResolver;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;

/**
 * Determines the binding scope of names.
 * 
 * Searches the scope in which the name appears for a 'global' statement or a
 * local binding. If the name is not bound locally nor locally declared to be
 * global, the decision falls to the enclosing namespace. This recursion
 * continues, trying each successive parent, until the name is determined to be
 * local or global or until the we reach the global namespace. At this point all
 * names are, by definition global.
 */
final class BindingScopeResolver extends LexicalResolver<CodeObject> {

    /**
     * Find the scope that a name in a given variable binds in.
     * 
     * Two things can determine the binding of a variable. Firstly, a 'global'
     * statement anywhere in the local scope will cause the variable to be
     * global and the name binds in the global namespace. Secondly, the variable
     * may be the subject of a binding operation in the current scope. If
     * neither of these is the case, the variable binds in the same scope
     * whatever the enclosing scope binds it in.
     * 
     * If the current scope is the global namespace, then the variable is bound
     * in that namespace. We don't need to search any further out than that
     * enclosing module as global declarations don't cross module boundaries.
     * 
     * @param variableName
     *            the variable being bound.
     * @param scope
     *            the scope within which the variable might appear
     * @return The binding scope of the name (either the current scope or the
     *         global namespace) if that could be determined. If not,
     *         {@code null} indicating that the determination should be
     *         delegated to the enclosing scope.
     */
    @Override
    protected CodeObject searchScopeForVariable(final String variableName,
            CodeObject scope) {

        ModuleCO containingModule = scope.enclosingModule();

        /*
         * If we've reached the global namespace or the global keyword appears,
         * the name must be a global, meaning that it is defined either in the
         * global namespace (i.e. the current module) or the builtin namespace.
         * 
         * Unlink other lexical bindings, the distinction between global
         * namespace and builtin namespace isn't statically determinable.
         * Instead the binding is said to be made in the conceptual 'top-level'
         * namespace. This means that the decision is made at runtime based on
         * whether the global namespace contains the token in question; if not,
         * it is requested from the builtin namespace.
         * 
         * We return the global namespace but this really means top-level
         * namespace.
         */
        boolean nameIsGlobal = scope.equals(containingModule)
                || scope.codeBlock().getGlobals().contains(variableName);
        if (nameIsGlobal) {
            return getGlobalNamespace(scope);
        } else if (isNameBoundInCodeBlock(variableName, scope.codeBlock())) {
            return scope;
        } else {
            return null;
        }

    }

    /**
     * Finds whether a variable is bound in the given code block.
     * 
     * This doesn't necessarily mean that it's a local variable of the block as
     * it may be the subject of a 'global' declaration in that block.
     * 
     * This function doesn't find bindings that occur in declarations such as
     * nested functions and classes that create a new code block.
     * 
     * @param variableName
     *            Variable whose binding we are searching for.
     * @param codeBlock
     *            Code block to search.
     */
    private static boolean isNameBoundInCodeBlock(final String variableName,
            final CodeBlock codeBlock) {

        return codeBlock.getBoundVariables().contains(variableName);
    }

    /**
     * Return the global namespace for the local scope.
     * 
     * In Python there is no truly 'global' namespace (other than __builtin__).
     * Instead the global namespace is simply the namespace of the module
     * containing the current scope.
     * 
     * If the current scope is a module then it is the global namespace so this
     * method returns the scope it is given.
     */
    private static CodeObject getGlobalNamespace(CodeObject scope) {

        return scope.enclosingModule();
    }
}
