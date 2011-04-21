package uk.ac.ic.doc.gander.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.python.pydev.parser.jython.SimpleNode;

public class Package implements Loadable {

	private String name;
	private Package parent;
	private boolean isSystem;

	private HashMap<String, Module> modules = new HashMap<String, Module>();
	private HashMap<String, Package> packages = new HashMap<String, Package>();
	private HashMap<String, Class> classes = new HashMap<String, Class>();
	private HashMap<String, Function> functions = new HashMap<String, Function>();

	public Package(org.python.pydev.parser.jython.ast.Module ast, String name,
			Package parent, boolean isSystem) {
		assert ast != null;

		this.name = name;
		this.parent = parent;
		this.isSystem = isSystem;
	}

	public Map<String, Package> getPackages() {
		return Collections.unmodifiableMap(packages);
	}

	public Map<String, Module> getModules() {
		return Collections.unmodifiableMap(modules);
	}

	public Map<String, Class> getClasses() {
		return Collections.unmodifiableMap(classes);
	}

	public Map<String, Function> getFunctions() {
		return Collections.unmodifiableMap(functions);
	}

	public void addPackage(Package subpackage) {
		packages.put(subpackage.getName(), subpackage);
	}

	public void addModule(Module submodule) {
		modules.put(submodule.getName(), submodule);
	}

	public void addClass(Class subclass) {
		classes.put(subclass.getName(), subclass);
	}

	public void addFunction(Function subfunction) {
		functions.put(subfunction.getName(), subfunction);
	}

	public boolean isSystem() {
		return isSystem;
	}

	public boolean isTopLevel() {
		return getParentPackage() == null;
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		if (isTopLevel())
			return getName();
		else {
			String parentName = parent.getFullName();
			if (parentName.isEmpty())
				return getName();
			else
				return parentName + "." + getName();
		}
	}

	public Module lookupModule(List<String> importNameTokens) {
		Queue<String> tokens = new LinkedList<String>(importNameTokens);

		Package scope = this;
		while (scope != null && !tokens.isEmpty()) {
			String token = tokens.remove();
			if (tokens.isEmpty())
				return scope.getModules().get(token);
			else
				scope = scope.getPackages().get(token);
		}

		return null;
	}

	public Package lookupPackage(List<String> importNameTokens) {
		Queue<String> tokens = new LinkedList<String>(importNameTokens);

		Package scope = this;
		while (scope != null && !tokens.isEmpty()) {
			String token = tokens.remove();
			if (tokens.isEmpty())
				return scope.getPackages().get(token);
			else
				scope = scope.getPackages().get(token);
		}

		return scope;
	}

	public Package getParentPackage() {
		return parent;
	}

	public Namespace getParentScope() {
		return getParentPackage();
	}

	@Override
	public String toString() {
		return "Package[" + getFullName() + "]";
	}

	public SimpleNode getAst() {
		// TODO Auto-generated method stub
		return null;
	}

}
