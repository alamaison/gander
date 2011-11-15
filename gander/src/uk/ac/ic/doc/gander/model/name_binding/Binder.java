package uk.ac.ic.doc.gander.model.name_binding;

import java.util.HashMap;
import java.util.Map;

import uk.ac.ic.doc.gander.model.LexicalTokenResolver;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;

/**
 * Although what a name is bound to is dynamically determined and not generally
 * solvable in Python, what scope that binding is looked up in <em>is</em>
 * statically determined.
 * 
 * In other words, given a name {@code x} appearing in a block of code, that
 * name won't, on some run of the program, refer to the value assigned to
 * {@code x} earlier in the block and, on another run, refer to the global
 * {@code x} or an {@code x} defined in an enclosing code block. For any name
 * there is always exactly one scope whose version of the name binding that name
 * might refer to. This class determines what that scope
 */
public final class Binder {

	/**
	 * A token in a particular scope (code object).
	 */
	private static class CodeObjectToken {
		private final String token;
		private final Namespace enclosingCodeObject;
		private final Model model;

		CodeObjectToken(String token, Namespace enclosingCodeObject, Model model) {
			this.token = token;
			this.enclosingCodeObject = enclosingCodeObject;
			this.model = model;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((enclosingCodeObject == null) ? 0 : enclosingCodeObject
							.hashCode());
			result = prime * result + ((model == null) ? 0 : model.hashCode());
			result = prime * result + ((token == null) ? 0 : token.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CodeObjectToken other = (CodeObjectToken) obj;
			if (enclosingCodeObject == null) {
				if (other.enclosingCodeObject != null)
					return false;
			} else if (!enclosingCodeObject.equals(other.enclosingCodeObject))
				return false;
			if (model == null) {
				if (other.model != null)
					return false;
			} else if (!model.equals(other.model))
				return false;
			if (token == null) {
				if (other.token != null)
					return false;
			} else if (!token.equals(other.token))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "CodeObjectToken [enclosingCodeObject="
					+ enclosingCodeObject + ", model=" + model + ", token="
					+ token + "]";
		}

	}

	private static final BindingScopeResolver RESOLVER = new BindingScopeResolver();
	private static final Map<CodeObjectToken, NamespaceKey> bindings = new HashMap<CodeObjectToken, NamespaceKey>();

	public static NamespaceKey resolveBindingScope(String name,
			Namespace enclosingCodeObject, Model model) {
		CodeObjectToken token = new CodeObjectToken(name, enclosingCodeObject,
				model);

		NamespaceKey binding = bindings.get(token);
		if (binding == null) {
			binding = new NamespaceKey(name, RESOLVER.resolveToken(name,
					enclosingCodeObject), model);
			bindings.put(token, binding);
		}

		return binding;
	}

	private Binder() {
	}

}

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
final class BindingScopeResolver extends LexicalTokenResolver<Namespace> {

	/**
	 * Find the scope that a name in a given scope binds in.
	 * 
	 * Two things can determine the binding of a name. Firstly, a 'global'
	 * statement anywhere in the local scope will cause the name to be global
	 * and the name binds in the global namespace. Secondly, the name may be the
	 * subject of a binding operation in the current scope. If neither of these
	 * is the case, the name binds to whatever the enclosing scope binds it to.
	 * 
	 * If the current scope is the global namespace, then the name is bound to
	 * that namespace. We don't need to search any further out than that
	 * enclosing module as global declarations don't cross module boundaries.
	 * 
	 * XXX: Is this true? What about submodules (subpackages really).
	 * 
	 * @param name
	 *            Name being bound.
	 * @param scope
	 *            Local (current) scope.
	 * @return The binding scope of the name (either the current scope or the
	 *         global namespace) if that could be determined. If not, {@code
	 *         null} indicating that the determination should be delegated to
	 *         the enclosing scope.
	 */
	@Override
	protected Namespace searchScopeForToken(final String name,
			final Namespace scope) {

		Namespace globalNamespace = getGlobalNamespace(scope);

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
		boolean nameIsGlobal = scope.equals(globalNamespace)
				|| scope.asCodeBlock().getGlobals().contains(name);
		if (nameIsGlobal) {
			return globalNamespace;
		} else if (isNameBoundInCodeBlock(name, scope.asCodeBlock())) {
			return scope;
		} else {
			return null;
		}

	}

	/**
	 * Finds whether a token is bound in the given code block.
	 * 
	 * This doesn't necessarily mean that it's a local variable of the block as
	 * it may be the subject of a 'global' declaration in that block.
	 * 
	 * This function doesn't find bindings that occur in declarations such as
	 * nested functions and classes that create a new code block.
	 * 
	 * @param name
	 *            Token whose binding we are searching for.
	 * @param codeBlock
	 *            Code block to search.
	 */
	private static boolean isNameBoundInCodeBlock(final String name,
			final CodeBlock codeBlock) {
		
		return codeBlock.getBoundVariables().contains(name);
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
	 * 
	 * XXX: Does the search look into a module's parent module (i.e. package)?
	 */
	private static Namespace getGlobalNamespace(Namespace localScope) {

		Namespace scope = localScope.getGlobalNamespace();

		assert scope instanceof Module;
		return scope;
	}
}
