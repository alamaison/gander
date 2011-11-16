package uk.ac.ic.doc.gander.flowinference;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;

public class SymbolTable {

	private Map<Namespace, Map<String, Type>> symbols = new HashMap<Namespace, Map<String, Type>>();

	private Model model;

	public SymbolTable(Model model) {
		this.model = model;
		processScope(model.getTopLevel());
	}

	public Map<String, Type> symbols(Namespace scope) {
		Map<String, Type> bindings = symbols.get(scope);
		if (bindings == null)
			bindings = Collections.emptyMap();

		return bindings;
	}

	private final class SymbolTableAstVisitor extends VisitorBase {

		private Namespace currentScope;

		SymbolTableAstVisitor(SimpleNode ast, Namespace currentScope) {
			this.currentScope = currentScope;
			try {
				ast.traverse(this);
			} catch (Exception e) {
				// No checked exceptions thrown by visit methods so any caught
				// here are unexpected
				throw new RuntimeException(e);
			}
		}

		@Override
		public Object visitClassDef(ClassDef node) throws Exception {
			Class scope = currentScope.getClasses().get(
					((NameTok) node.name).id);
			// FIXME: Make this assert pass with conditional function decls
			// assert node == scope.getClassDef();
			if (scope == null)
				throw new Error("Class found while scoping that doesn't "
						+ "exist in the model: " + ((NameTok) node.name).id);
			return null;

			// Do not traverse. This has already been taken care of while
			// building the model. processScope() does the equivalent of
			// traversing the elements using the model instead of the AST.
		}

		@Override
		public Object visitFunctionDef(FunctionDef node) throws Exception {
			Function scope = currentScope.getFunctions().get(
					((NameTok) node.name).id);
			// FIXME: Make this assert pass with conditional function decls
			// assert node == scope.getFunctionDef();
			if (scope == null)
				throw new Error("Function found while scoping that doesn't "
						+ "exist in the model: " + ((NameTok) node.name).id);
			return null;

			// Do not traverse. This has already been taken care of while
			// building the model. processScope() does the equivalent of
			// traversing the elements using the model instead of the AST.
		}

		@Override
		public Object visitImport(Import node) throws Exception {
			try {
				new ImportSymbols(currentScope).resolveImport(node);
			} catch (UnresolvedImportError e) {
			}
			return null;
		}

		@Override
		public Object visitImportFrom(ImportFrom node) throws Exception {
			try {
				new ImportSymbols(currentScope).resolveImportFrom(node);
			} catch (UnresolvedImportError e) {
			}
			return null;
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			// Traverse everywhere except function and class definition nodes as
			// they have their own symbol tables and are processed separately
			// using the model.
			node.traverse(this);
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {
			return null;
		}
	}

	private class ImportSymbols {
		private Namespace currentScope;

		ImportSymbols(Namespace currentScope) {
			this.currentScope = currentScope;
		}

		void resolveImport(Import node) {
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
					new SymbolTableImportResolver(model, currentScope)
							.simulateImportAs(((NameTok) alias.name).id,
									((NameTok) alias.asname).id);
				} else {
					new SymbolTableImportResolver(model, currentScope)
							.simulateImport(((NameTok) alias.name).id);
				}
			}
		}

		void resolveImportFrom(ImportFrom node) throws Exception {

			for (aliasType alias : node.names) {
				if (alias.asname != null) {
					new SymbolTableImportResolver(model, currentScope)
							.simulateImportFromAs(((NameTok) node.module).id,
									((NameTok) alias.name).id,
									((NameTok) alias.asname).id);
				} else {
					new SymbolTableImportResolver(model, currentScope)
							.simulateImportFrom(((NameTok) node.module).id,
									((NameTok) alias.name).id);
				}
			}

		}

	}

	private class SymbolTableImportResolver extends ImportTyper {

		public SymbolTableImportResolver(Model model, Namespace importReceiver) {
			super(model, importReceiver);
		}

		@Override
		protected void put(Namespace scope, String name, Type type) {
			SymbolTable.this.put(scope, name, type);
		}

	}

	private void processScope(Namespace scope) {
		new SymbolTableAstVisitor(scope.getAst(), scope);

		for (Module pkg : scope.getModules().values()) {
			// SourceFile must be part of existing runtime model.
			// If it weren't then we couldn't guarantee that the modules it
			// imports would already have been loaded. This is something we rely
			// on when resolving the import name to a parsed SourceFile object
			// later.
			assert model.lookup(pkg.getFullName()) != null;

			processScope(pkg);
		}

		for (Class klass : scope.getClasses().values()) {
			put(scope, klass.getName(), new TClass(klass));

			processScope(klass);
		}

		for (Function function : scope.getFunctions().values()) {
			// Methods are not in the symbol table of their enclosing class.
			// They can only be accessed by dereferencing 'self'
			// TODO: Does it make sense to add a 'self' token to the symbol
			// table of the methods? What type would it have?
			if (!(scope instanceof Class))
				put(scope, function.getName(), new TFunction(function));

			processScope(function);
		}
	}

	private void put(Namespace scope, String name, Type type) {
		assert type != null;

		Map<String, Type> scopeMapping = symbols.get(scope);
		if (scopeMapping == null) {
			scopeMapping = new HashMap<String, Type>();
			symbols.put(scope, scopeMapping);
		}

		scopeMapping.put(name, type);
	}
}
