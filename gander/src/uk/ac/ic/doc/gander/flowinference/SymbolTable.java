package uk.ac.ic.doc.gander.flowinference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.VisitorBase;
import org.python.pydev.parser.jython.ast.aliasType;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.TPackage;
import uk.ac.ic.doc.gander.flowinference.types.TUnresolvedImport;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.importing.ImportSimulator;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Loadable;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.Package;

public class SymbolTable {

	private Map<Namespace, Map<String, Type>> symbols = new HashMap<Namespace, Map<String, Type>>();

	private Model model;

	public SymbolTable(Model model) {
		this.model = model;
		processScope(model.getTopLevelPackage());
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
					new ImportResolver(currentScope, model.getTopLevelPackage())
							.simulateImportAs(((NameTok) alias.name).id,
									((NameTok) alias.asname).id);
				} else {
					new ImportResolver(currentScope, model.getTopLevelPackage())
							.simulateImport(((NameTok) alias.name).id);
				}
			}
		}

		void resolveImportFrom(ImportFrom node) throws Exception {

			for (aliasType alias : node.names) {
				if (alias.asname != null) {
					new ImportResolver(currentScope, model.getTopLevelPackage())
							.simulateImportFromAs(((NameTok) node.module).id,
									((NameTok) alias.name).id,
									((NameTok) alias.asname).id);
				} else {
					new ImportResolver(currentScope, model.getTopLevelPackage())
							.simulateImportFrom(((NameTok) node.module).id,
									((NameTok) alias.name).id);
				}
			}

		}

	}

	private class ImportResolver extends ImportSimulator {
		private ImportResolver(Namespace importReceiver, Package topLevel) {
			super(importReceiver, topLevel);
		}

		@Override
		protected void bindName(Namespace importReceiver, Namespace loaded,
				String as) {
			assert loaded != null;
			assert importReceiver != null;
			assert !as.isEmpty();

			Type type = null;
			if (loaded instanceof Package)
				type = new TPackage((Package) loaded);
			else if (loaded instanceof Module)
				type = new TModule((Module) loaded);
			else if (loaded instanceof Class)
				type = new TClass((Class) loaded);
			else if (loaded instanceof Function)
				type = new TFunction((Function) loaded);

			// TODO: The target of the 'from foo import bar' can
			// be a variable.

			put(importReceiver, as, type);
		}

		@Override
		protected Loadable simulateLoad(List<String> importPath,
				Package relativeToPackage) {
			List<String> name = new ArrayList<String>(DottedName
					.toImportTokens(relativeToPackage.getFullName()));
			name.addAll(importPath);

			// The imported module/package will always exist in the model
			// already if it exists (on disk) at all as the model must have
			// tried to import it already. Therefore we only do a lookup here
			// rather than attempting a load.
			Loadable loaded = model.lookupPackage(name);
			if (loaded == null)
				loaded = model.lookupModule(name);

			return loaded;
		}

		@Override
		protected void onUnresolvedImport(List<String> importPath,
				Package relativeToPackage, Namespace importReceiver, String as) {

			put(importReceiver, as, new TUnresolvedImport(importPath,
					relativeToPackage));
		}

		@Override
		protected void onUnresolvedImportFrom(List<String> fromPath,
				String itemName, Package relativeToPackage,
				Namespace importReceiver, String as) {

			put(importReceiver, as, new TUnresolvedImport(fromPath, itemName,
					relativeToPackage));
		}
	}

	private void processScope(Namespace scope) {
		new SymbolTableAstVisitor(scope.getAst(), scope);

		for (Package pkg : scope.getPackages().values()) {
			// Loadable must be part of existing runtime model.
			// If it weren't then we couldn't guarantee that the modules it
			// imports would already have been loaded. This is something we rely
			// on when resolving the import name to a parsed Module object
			// later.
			assert model.lookupPackage(pkg.getFullName()) != null;

			processScope(pkg);
		}

		for (Module module : scope.getModules().values()) {
			// Loadable must be part of existing runtime model.
			// If it weren't then we couldn't guarantee that the modules it
			// imports would already have been loaded. This is something we rely
			// on when resolving the import name to a parsed Module object
			// later.
			assert model.lookupModule(module.getFullName()) != null;

			processScope(module);
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
