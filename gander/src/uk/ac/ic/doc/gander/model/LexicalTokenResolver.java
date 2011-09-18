package uk.ac.ic.doc.gander.model;

/**
 * Search for token following the rules of lexical scoping.
 * 
 * Subclass this class to implement the specific search scheme.
 * 
 * The class is parameterised with the class of object being resolved. This
 * allows the class to be used to resolve the token to arbitrary objects such as
 * model elements or inferred types. This class doesdn't dictate that aspect of
 * the search and leaves it to subclasses to decide what makes sense.
 * 
 * TODO: Does this really follow the rules? Does Python escalate the search
 * scope arbitrarily like this or does it have special rules for classes,
 * modules, etc?
 */
public abstract class LexicalTokenResolver<T> {

	/**
	 * Find the type of the given token.
	 * 
	 * @param token
	 *            The token whose type to find.
	 * @param enclosingScope
	 *            The scope in which to start the search.
	 */
	public final T resolveToken(String token, Namespace enclosingScope) {
		T type = null;

		if (enclosingScope != null) {
			type = searchScopeForToken(token, enclosingScope);
			if (type == null)
				type = resolveToken(token, nextScopeToSearch(enclosingScope));
		}

		return type;
	}

	/**
	 * Try to find a type for the given token in the given rootScope.
	 * 
	 * Do not look outside that rootScope. If unable to find a type, return
	 * null.
	 */
	protected abstract T searchScopeForToken(String token, Namespace scope);

	private Namespace nextScopeToSearch(Namespace currentScope) {

		Namespace parentScope = currentScope.getParentScope();

		/*
		 * The lexical scoping rules in Python aren't orthodox. Names defined in
		 * a class body don't have scope outside the class's code block. Names
		 * used in nested classes or functions (methods) aren't bound to any
		 * matching name in the class scope. Instead they are bound to the next
		 * enclosing non-class scope.
		 */
		if (parentScope instanceof Class) {
			return nextScopeToSearch(parentScope); // skip this one
		} else {
			return parentScope;
		}
	}
}