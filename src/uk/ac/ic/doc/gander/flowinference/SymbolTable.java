package uk.ac.ic.doc.gander.flowinference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.VisitorBase;
import org.python.pydev.parser.jython.ast.aliasType;

import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.TPackage;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Package;
import uk.ac.ic.doc.gander.model.Scope;

public class SymbolTable {

	private Stack<Scope> scopes = new Stack<Scope>();

	private Map<Scope, Map<String, Type>> symbols = new HashMap<Scope, Map<String, Type>>();

	private Model model;

	public SymbolTable(Model model) throws Exception {
		this.model = model;
		processScope(model.getTopLevelPackage());
	}

	public Map<String, Type> symbols(Scope scope) {
		Map<String, Type> bindings = symbols.get(scope);
		if (bindings == null)
			bindings = Collections.emptyMap();

		return bindings;
	}

	private class SymbolTableAstVisitor extends VisitorBase {

		@Override
		public Object visitClassDef(ClassDef node) throws Exception {
			Class scope = scopes.peek().getClasses().get(
					((NameTok) node.name).id);
			assert node == scope.getClassDef();
			if (scope == null)
				throw new Error("Class found while scoping that doesn't "
						+ "exist in the model: " + ((NameTok) node.name).id);

			// Do not traverse. This has already been taken care of while
			// building the model.
			return null;
		}

		@Override
		public Object visitFunctionDef(FunctionDef node) throws Exception {
			Function scope = scopes.peek().getFunctions().get(
					((NameTok) node.name).id);
			// FIXME: Make this assert pass
			// assert node == scope.getFunctionDef();
			if (scope == null)
				throw new Error("Function found while scoping that doesn't "
						+ "exist in the model: " + ((NameTok) node.name).id);

			// Do not traverse. This has already been taken care of while
			// building the model.
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
					simulateImportAs(((NameTok) alias.name).id,
							((NameTok) alias.asname).id);
				} else {
					simulateImport(((NameTok) alias.name).id);
				}
			}
			return null;
		}

		@Override
		public Object visitImportFrom(ImportFrom node) throws Exception {
			Module module = model.lookupModule(((NameTok) node.module).id);
			if (module != null) {
				for (aliasType alias : node.names) {
					if (alias.asname != null) {
						simulateImportFromAs(module, ((NameTok) alias.name).id,
								((NameTok) alias.asname).id);
					} else {
						simulateImportFrom(module, ((NameTok) alias.name).id);
					}
				}
			}
			return null;
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			node.traverse(this);
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {
			return null;
		}

		private void simulateImportFrom(Module module, String itemName) {
			simulateImportFromAs(module, itemName, itemName);
		}

		private void simulateImportFromAs(Module module, String itemName,
				String asName) {
			Type type = null;

			// Resolve item name to an item inside the module
			Package pkg = module.getPackages().get(itemName);
			if (pkg != null) {
				type = new TPackage(pkg);
			} else {
				Module submodule = module.getModules().get(itemName);
				if (submodule != null) {
					type = new TModule(submodule);
				} else {
					Class klass = module.getClasses().get(itemName);
					if (klass != null) {
						type = new TClass(klass);
					} else {
						Function function = module.getFunctions().get(itemName);
						if (function != null) {
							type = new TFunction(function);
						}
						
						// TODO: The target of the 'from foo import bar' can
						// be a variable.
					}
				}
			}

			if (type != null) {
				put(scopes.peek(), asName, type);
			}
		}

		private void simulateImportAs(String importName, String asName) {
			Module module = model.lookupModule(importName);
			if (module != null) {
				put(scopes.peek(), asName, new TModule(module));
			}
		}

		private void simulateImport(String importName) {
			Queue<String> tokens = new LinkedList<String>(
					dottedNameToImportTokens(importName));

			Scope scope = scopes.peek();
			List<String> processed = new ArrayList<String>();
			while (!tokens.isEmpty()) {
				String token = tokens.remove();
				processed.add(token);

				if (!tokens.isEmpty()) {
					Package pkg = model.lookupPackage(processed);
					put(scope, token, new TPackage(pkg));

					scope = pkg;
				} else {
					Module module = model.lookupModule(processed);
					put(scope, token, new TModule(module));
				}
			}
		}

	}

	private void processScope(Scope scope) throws Exception {
		scopes.push(scope);

		for (Package pkg : scope.getPackages().values()) {
			processScope(pkg);
		}

		for (Module module : scope.getModules().values()) {
			scopes.push(module);
			module.getAst().accept(new SymbolTableAstVisitor());
			scopes.pop();

			processScope(module);
		}

		for (Class klass : scope.getClasses().values()) {
			put(scopes.peek(), klass.getName(), new TClass(klass));
			processScope(klass);
		}

		for (Function function : scope.getFunctions().values()) {
			put(scopes.peek(), function.getName(), new TFunction(function));
			processScope(function);
		}

		scopes.pop();
	}

	private static List<String> dottedNameToImportTokens(String importPath) {
		return Arrays.asList(importPath.split("\\."));
	}

	private void put(Scope scope, String name, Type type) {
		Map<String, Type> scopeMapping = symbols.get(scope);
		if (scopeMapping == null) {
			scopeMapping = new HashMap<String, Type>();
			symbols.put(scope, scopeMapping);
		}

		scopeMapping.put(name, type);
	}
}
