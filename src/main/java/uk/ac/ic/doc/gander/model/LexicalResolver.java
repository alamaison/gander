package uk.ac.ic.doc.gander.model;

import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

/**
 * Resolver that establishes a binding for variables in a code object following
 * the rules of lexical scoping.
 * 
 * Subclass this class to implement the specific search scheme. Exactly what is
 * being bound is not dictated by this class. It is, instead, parameterised with
 * the type of the binding result, allowing the class to be used to resolve the
 * variable to arbitrary objects such as model elements or inferred types.
 */
public abstract class LexicalResolver<T> {

    /**
     * Resolve the given variable.
     * 
     * @param variable
     *            the name of the variable to resolve
     * @param codeObject
     *            the scope in which to start the search; initially this is the
     *            leaf code object, not the root
     */
    public final T resolveToken(String variable, CodeObject codeObject) {
        if (codeObject == null) {
            throw new NullPointerException(
                    "Node code object to resolve variable in");
        }

        T type = searchScopeForVariable(variable, codeObject);
        if (type == null) {
            CodeObject nextScope = nextScopeToSearch(codeObject);
            if (nextScope != null) {
                type = resolveToken(variable, nextScope);
            }
        }

        return type;
    }

    /**
     * Try to find a binding for the given variable in the given code object's
     * code block.
     * 
     * Do not look outside that code block. If unable to find a type, return
     * {@code null}.
     */
    protected abstract T searchScopeForVariable(String variable,
            CodeObject scope);

    /**
     * Returns which code object we should search next for a binding, assuming
     * our variable does not bind in the given code object.
     * 
     * @param nonBindingCodeObject
     *            the code object whose namespace we know our variable does not
     *            bind in
     * @return the code object whose namespace we should search next or {@code
     *         null} if no further search is possible
     */
    private CodeObject nextScopeToSearch(CodeObject nonBindingCodeObject) {

        return nonBindingCodeObject.lexicallyNextCodeObject();
    }
}