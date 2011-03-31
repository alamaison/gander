package uk.ac.ic.doc.gander.model.build;

import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.FunctionDef;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.Package;

public abstract class ModuleNamespaceBuilder extends ScopedVisitor<Namespace> {

	private String moduleName;

	public ModuleNamespaceBuilder(String moduleName, Package parent) {
		super(parent);
		this.moduleName = moduleName;
	}

	protected void onCreatedModule(Module module) {
	}

	protected void onCreatedClass(Class klass) {
	}

	protected void onCreatedFunction(Function function) {
	}

	@Override
	protected final Namespace createScope(
			org.python.pydev.parser.jython.ast.Module node) {
		Module m = new Module(node, moduleName, (Package) getScope());
		onCreatedModule(m);
		return m;
	}

	@Override
	protected final Namespace createScope(ClassDef node) {
		Class c = new Class(node, getScope());
		onCreatedClass(c);
		return c;
	}

	@Override
	protected final Namespace createScope(FunctionDef node) {
		Function f = new Function(node, getScope());
		onCreatedFunction(f);
		return f;
	}
}