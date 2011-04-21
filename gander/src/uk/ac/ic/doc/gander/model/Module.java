package uk.ac.ic.doc.gander.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Module implements Loadable {

	private Map<String, Class> classes = new HashMap<String, Class>();
	private Map<String, Function> functions = new HashMap<String, Function>();

	private String name;
	private org.python.pydev.parser.jython.ast.Module ast;
	private Package parent;
	private boolean isSystem;

	public Module(org.python.pydev.parser.jython.ast.Module ast, String name,
			Package parent, boolean isSystem) {
		assert ast != null;
		assert parent != null;
		assert !name.isEmpty();

		this.ast = ast;
		this.name = name;
		this.parent = parent;
		this.isSystem = isSystem;
	}

	public boolean isSystem() {
		return isSystem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ic.doc.cfg.model.IModelElement#getName()
	 */
	public String getName() {
		return name;
	}

	public String getFullName() {
		String parentName = parent.getFullName();
		if (parentName.isEmpty())
			return getName();
		else
			return parentName + "." + getName();
	}

	public Package getParentPackage() {
		return parent;
	}

	public Namespace getParentScope() {
		return getParentPackage();
	}

	public Map<String, Package> getPackages() {
		return Collections.emptyMap();
	}

	public Map<String, Module> getModules() {
		return Collections.emptyMap();
	}

	public Map<String, Class> getClasses() {
		return Collections.unmodifiableMap(classes);
	}

	public Map<String, Function> getFunctions() {
		return Collections.unmodifiableMap(functions);
	}

	public void addPackage(Package pkg) {
		throw new Error("A module cannot contain a package");
	}

	public void addModule(Module module) {
		throw new Error("A module cannot contain another module");
	}

	public void addClass(Class klass) {
		classes.put(klass.getName(), klass);
	}

	public void addFunction(Function function) {
		functions.put(function.getName(), function);
	}

	@Override
	public String toString() {
		return "Module[" + getFullName() + "]";
	}

	public org.python.pydev.parser.jython.ast.Module getAst() {
		return ast;
	}
}
