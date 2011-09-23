package uk.ac.ic.doc.gander.model.name_binding;

import java.util.HashSet;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.For;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Global;
import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.NameTokType;
import org.python.pydev.parser.jython.ast.TryExcept;
import org.python.pydev.parser.jython.ast.excepthandlerType;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ast.BindingStatementVisitor;
import uk.ac.ic.doc.gander.ast.LocalCodeBlockVisitor;
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

		if (scope.equals(globalNamespace)) {
			/*
			 * If we've reached the global namespace the name but be a global.
			 * We don't even check for it in the global namespace as there's
			 * nothing else it could be .. except wrong.
			 */
			/*
			 * TODO: Is this right? Is there some way to check and report an
			 * error if no such global exists?
			 */
			return scope;

		} else if (isNameDeclaredGlobalInScope(name, scope)) {
			return globalNamespace;

		} else if (isNameBoundInScope(name, scope)) {
			return scope;

		} else {
			return null;
		}

	}

	/**
	 * Finds whether a token is bound in the given scope.
	 * 
	 * This doesn't necessarily mean that it's a local variable of the scope as
	 * it may be the subject of a 'global' declaration in that scope.
	 * 
	 * @param name
	 *            Token whose binding we are searching for.
	 * @param scope
	 *            Scope of the search.
	 */
	private boolean isNameBoundInScope(final String name, final Namespace scope) {

		/*
		 * TODO: decide whether we want to search for one name or all names.
		 * They have different performance trade-offs depending on the
		 * application.
		 */

		final java.util.Set<String> boundNames = new HashSet<String>();

		CodeBlock codeBlock = scope.asCodeBlock();

		boundNames.addAll(codeBlock.getFormalParameters());

		try {

			/*
			 * It might be possible to do this just by overriding visitName and
			 * visitNameTok and looking at their contexts to decide if they are
			 * being used in a binding context but, for the moment, we do it the
			 * long way
			 */

			/*
			 * After extracting the bound names at each node we traverse the
			 * node because, if they have a body like a for-loop, they may nest
			 * other definitions.
			 */
			codeBlock.accept(new BindingStatementVisitor() {

				@Override
				public Object visitTryExcept(TryExcept node) throws Exception {
					for (excepthandlerType handler : node.handlers) {
						if (handler.name instanceof Name) {
							boundNames.add(((Name) handler.name).id);
						} else {
							// XXX: No idea what happens here. How could the
							// name of the exception object _not_ be a name?
						}
					}

					node.traverse(this);
					return null;
				}

				@Override
				public Object visitImportFrom(ImportFrom node) throws Exception {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Object visitImport(Import node) throws Exception {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Object visitFunctionDef(FunctionDef node)
						throws Exception {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Object visitFor(For node) throws Exception {
					if (node.target instanceof Name) {
						boundNames.add(((Name) node.target).id);
					} else {
						// XXX: No idea what happens here. How could the
						// for-loop variable _not_ be a name?
					}

					node.traverse(this);
					return null;
				}

				@Override
				public Object visitClassDef(ClassDef node) throws Exception {
					if (node.name instanceof NameTok) {
						boundNames.add(((NameTok) node.name).id);
					} else {
						// XXX: No idea what happens here. How could the
						// class name _not_ be a name?
					}

					// Do NOT recurse into the ClassDef body. Despite
					// appearances, it is not part of this scope's code block.
					// It is a declaration of the class scope's code block.
					// Another way to think about it: the class's body is not
					// being 'executed' now whereas the enclosing namespace's
					// body is.

					return null;
				}

				@Override
				public Object visitAssign(Assign node) throws Exception {
					for (exprType lhsExpression : node.targets) {
						if (lhsExpression instanceof Name) {
							boundNames.add(((Name) lhsExpression).id);
						}
					}

					node.traverse(this);
					return null;
				}

				@Override
				public void traverse(SimpleNode node) throws Exception {
					// Traverse by default so that we catch all assignments even
					// if they are nested
					node.traverse(this);
				}

				@Override
				protected Object unhandled_node(SimpleNode node)
						throws Exception {
					return null;
				}

			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return boundNames.contains(name);
	}

	/**
	 * Determines if the given name is declared global in the local scope.
	 * 
	 * This only returns {@code true} if the given scope declares the name to be
	 * global. The name may <em>be</em> globally binding if a parent scope
	 * declares it so but this method only looks at the given scope. If it is
	 * declared global in a nested scope that also won't affect the result of
	 * this method as these declarations don't affect global binding for their
	 * parents.
	 */
	private boolean isNameDeclaredGlobalInScope(final String name,
			final Namespace scope) {

		/*
		 * The 'global' statement can occur at any point in a code block but
		 * it's effect covers the entire block (the spec says it must appear
		 * before the name is used but CPython only generates a warning).
		 * Therefore we don't try to establish that is precedes all mentions of
		 * the name.
		 */

		// Declared in this bizarre way so the anonymous inner can assign to it
		final boolean[] tokenIsGlobal = { false };

		try {

			/*
			 * Using a LocalCodeBlock visitor as a global statement in a nested
			 * class or function doesn't affect the enclosing scope's binding
			 * 
			 * The moment we find the first matching global declaration, it is
			 * time to finish as any others are redundant. We can't return
			 * prematurely from the visitor so we use a flag to know we're
			 * finished and short-cut the visitor's work.
			 */
			scope.asCodeBlock().accept(new LocalCodeBlockVisitor() {

				@Override
				public Object visitGlobal(Global node) throws Exception {
					if (!tokenIsGlobal[0]) {
						for (NameTokType tok : node.names) {
							if (((NameTok) tok).id.equals(name)) {
								tokenIsGlobal[0] = true;
								break;
							}
						}
					}
					return null;
				}

				@Override
				protected Object unhandled_node(SimpleNode node)
						throws Exception {
					return null;
				}

				@Override
				public void traverse(SimpleNode node) throws Exception {
					/*
					 * traverse by default because the 'global' statement might
					 * be nested, for instance, in a loop or conditional
					 */
					if (!tokenIsGlobal[0]) {
						node.traverse(this);
					}
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return tokenIsGlobal[0];
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
}
