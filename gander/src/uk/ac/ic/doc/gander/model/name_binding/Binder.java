package uk.ac.ic.doc.gander.model.name_binding;

import java.util.HashSet;

import uk.ac.ic.doc.gander.model.CodeBlock;
import uk.ac.ic.doc.gander.model.LexicalTokenResolver;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;

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

	private static final BindingScopeResolver RESOLVER = new BindingScopeResolver();

	public Namespace resolveBindingScope(String name, Namespace enclosingScope) {
		return RESOLVER.resolveToken(name, enclosingScope);
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

		/* TODO: Don't recompute every time */
		Namespace globalNamespace = getGlobalNamespace(scope);
		Namespace builtinNamespace = getBuiltinNamespace(scope);

		/*
		 * If we've reached the global namespace or the global keyword appears,
		 * the name must be a global, meaning that it is defined either in the
		 * global namespace (i.e. the current module) or the builtin namespace.
		 */
		boolean nameIsGlobal = scope.equals(globalNamespace)
				|| scope.asCodeBlock().getGlobals().contains(name);
		if (nameIsGlobal) {
			if (isNameBoundInModule(name, globalNamespace)) {
				return globalNamespace;
			} else {
				/*
				 * We don't even check for it in the builtin namespace as
				 * there's nothing else it could be .. except wrong.
				 */
				/*
				 * TODO: Is this right? Is there some way to check and report an
				 * error if no such global exists?
				 */
				return builtinNamespace;
			}
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

		/*
		 * TODO: decide whether we want to search for one name or all names.
		 * They have different performance trade-offs depending on the
		 * application.
		 */

		final java.util.Set<String> boundNames = new HashSet<String>();

		boundNames.addAll(codeBlock.getFormalParameters());

		try {
			codeBlock.accept(new LocallyBoundNameFinder() {

				@Override
				protected void onNameBound(String name) {
					boundNames.add(name);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return boundNames.contains(name);
	}

	private static boolean isNameBoundInModule(final String name,
			final Namespace module) {

		/*
		 * TODO: decide whether we want to search for one name or all names.
		 * They have different performance trade-offs depending on the
		 * application.
		 */

		final java.util.Set<String> boundNames = new HashSet<String>();

		try {
			module.getAst().accept(new BoundNameFinder() {

				@Override
				protected void onNameBound(String name) {
					boundNames.add(name);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return boundNames.contains(name);
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
		Namespace scope = localScope;
		while (!(scope instanceof Module)) {
			scope = scope.getParentScope();
		}

		assert scope instanceof Module;
		return scope;
	}

	/**
	 * Return the model's representation of the __builtin__ namespace.
	 */
	private static Namespace getBuiltinNamespace(Namespace localScope) {
		Namespace scope = getGlobalNamespace(localScope);
		assert scope != null;

		while (true) {
			Namespace parent = scope.getParentScope();
			if (parent == null) {
				assert scope instanceof Module;
				return scope;
			} else {
				scope = parent;
			}
		}
	}
}
