package uk.ac.ic.doc.gander.flowinference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.Module;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.VisitorBase;
import org.python.pydev.parser.jython.ast.aliasType;

import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Scope;

public class TypeResolver extends VisitorBase {

	// XXX: We are mapping strings to types. This isn't enough for real code.
	// As well as the string name of the token we're resolving, we need the
	// scope in which it appears as different names in different scopes
	// may refer to completely different things.
	private Map<Scope, Map<String, Type>> inferredTypes = new HashMap<Scope, Map<String, Type>>();
	private Scope scope = null;
	private uk.ac.ic.doc.gander.model.Module module;

	public TypeResolver(Model model, uk.ac.ic.doc.gander.model.Module module)
			throws Exception {
		this.module = module;
		module.getAst().accept(new TypeCollector());
	}

	private class TypeCollector extends VisitorBase {

		private Stack<Scope> scopes = new Stack<Scope>();

		@Override
		public Object visitClassDef(ClassDef node) throws Exception {
			uk.ac.ic.doc.gander.model.Class scope = scopes.peek().getClasses().get(
					((NameTok) node.name).id);
			if (scope == null)
				throw new Error("Class found while scoping that doesn't "
						+ "exist in the model: " + ((NameTok) node.name).id);

			assert node == scope.getClassDef();
			scopes.push(scope);
			node.traverse(this);
			scopes.pop();
			return null;
		}

		@Override
		public Object visitFunctionDef(FunctionDef node) throws Exception {
			Function scope = scopes.peek().getFunctions().get(
					((NameTok) node.name).id);
			if (scope == null)
				throw new Error("Function found while scoping that doesn't "
						+ "exist in the model: " + ((NameTok) node.name).id);

			assert node == scope.getFunctionDef();
			scopes.push(scope);
			node.traverse(this);
			scopes.pop();
			return null;
		}

		@Override
		public Object visitModule(Module node) throws Exception {
			assert node == module.getAst();
			scopes.push(module);
			node.traverse(this);
			scopes.pop();
			assert scopes.isEmpty();
			return null;
		}

		@Override
		public Object visitImport(Import node) throws Exception {
			// The import name is a string which may contain dots to indicate
			// a package module as in "package.subpackage.submodule". Python
			// will import each segment of the name as a package before
			// importing the module. In other words Python will import:
			//
			// package
			// package.subpackage
			// package.subpackage.submodule
			//
			// If the import statement uses 'as' to alias the module, the
			// submodule is bound directly to the 'as' token in the local
			// namespace. Otherwise, only the first segment ('package') is
			// bound in the local namespace and later parts are bound to names
			// inside the bound module! In other words, 'import x.y.x' loads
			// x and binds it to name 'x' in the local namespace, then loads x.y
			// and binds it to name 'y' in the loaded x, then finally loads
			// x.y.z and binds that to 'z' in the loaded x.y.
			//
			// XXX: WARNING: This behaviour seems very odd (importing a package
			// changes the contents of modules globally!) and I can't find any
			// documentation that confirms it. But trying it out in Python
			// shows me it works this way.
			//
			// We have to simulate the same thing by breaking the import name
			// apart and each to the inferred types
			for (aliasType alias : node.names) {
				if (alias.asname != null) {
					put(scopes.peek(), ((NameTok) alias.asname).id,
							new TModule());
				} else {

					Scope currentScope = scopes.peek();
					List<String> tokens = importTokens(((NameTok) alias.name).id);
					for (String token : tokens) {
						put(currentScope, token, new TModule());
						/*
						 * currentScope = currentScope.lookup(token); if
						 * (currentScope == null) { System.err
						 * .println("WARNING: failed to find token '" + token +
						 * "'"); break; }
						 */
					}

				}
			}
			return null;
		}

		private List<String> importTokens(String importPath) {
			return Arrays.asList(importPath.split("\\."));
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			node.traverse(this);
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {
			return null;
		}

		private void put(Scope scope, String name, Type type) {
			Map<String, Type> scopeMapping = inferredTypes.get(scope);
			if (scopeMapping == null) {
				scopeMapping = new HashMap<String, Type>();
				inferredTypes.put(scope, scopeMapping);
			}

			scopeMapping.put(name, type);
		}
	}

	public Type typeOf(SimpleNode node, Scope scope) throws Exception {
		assert this.scope == null;
		try {
			this.scope = scope;
			Type type = (Type) node.accept(this);
			this.scope = null;
			return type;
		} finally {
			this.scope = null;
		}
	}

	@Override
	public Object visitName(Name node) throws Exception {
		return resolveTokenInScopeChain(node.id);
	}

	@Override
	public void traverse(SimpleNode node) throws Exception {
		// Don't traverse by default
	}

	@Override
	protected Object unhandled_node(SimpleNode node) throws Exception {
		return null;
	}

	private Type resolveTokenInScopeChain(String token) {
		return resolveTokenInScopeRecursively(token, scope);
	}

	private Type resolveTokenInScopeRecursively(String token, Scope scope) {
		Type type = null;

		Map<String, Type> scopeTokens = inferredTypes.get(scope);
		if (scopeTokens != null)
			type = scopeTokens.get(token);
		if (type == null && scope != null)
			type = resolveTokenInScopeRecursively(token, scope.getParentScope());

		return type;
	}
}
