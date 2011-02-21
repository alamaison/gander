package uk.ac.ic.doc.gander.model;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.python.pydev.parser.jython.ParseException;

import uk.ac.ic.doc.gander.model.build.BuildableScope;

public class Package implements Scope, BuildableScope {

	private HashMap<String, Module> modules = new HashMap<String, Module>();
	private HashMap<String, Package> packages = new HashMap<String, Package>();
	private String name;
	private Package parent = null;

	public Package(String name, Package parent) throws IOException,
			ParseException, InvalidElementException {
		this.name = name;
		this.parent = parent;
	}

	private boolean isTopLevel() {
		return parent == null;
	}

	public void addPackage(Package subpackage) {
		packages.put(subpackage.getName(), subpackage);
	}

	public void addModule(Module submodule) {
		modules.put(submodule.getName(), submodule);
	}

	public void addClass(Class klass) {
		// TODO: implement
		throw new Error("Not implemented");
	}

	public void addFunction(Function function) {
		// TODO: implement
		throw new Error("Not implemented");
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
		if (isTopLevel())
			return getName();
		else
			return parent.getFullName() + "." + getName();
	}

	public Map<String, Package> getPackages() {
		return packages;
	}

	public Map<String, Module> getModules() {
		return modules;
	}

	public Scope lookup(String token) {
		Scope subItem = getPackages().get(token);
		if (subItem == null)
			subItem = getModules().get(token);

		return subItem;
	}

	public Package getParentPackage() {
		return parent;
	}

	public Scope getParentScope() {
		return getParentPackage();
	}

	public Map<String, Class> getClasses() {
		// TODO: implement
		return Collections.emptyMap();
	}

	public Map<String, Function> getFunctions() {
		// TODO: implement
		return Collections.emptyMap();
	}
}
