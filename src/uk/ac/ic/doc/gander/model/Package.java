package uk.ac.ic.doc.gander.model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.python.pydev.parser.jython.ParseException;

import uk.ac.ic.doc.gander.model.build.ModuleParser;


public class Package implements Scope {

	private HashMap<String, Module> modules = new HashMap<String, Module>();
	private HashMap<String, Package> packages = new HashMap<String, Package>();
	private String name;
	private Package parent = null;

	private static final String PACKAGE_TAG_NAME = "__init__";

	public Package(File directory) throws IOException, ParseException,
			InvalidElementException {
		processDirectory(directory);
	}

	private Package(File directory, Package parent) throws IOException,
			ParseException, InvalidElementException {
		this.parent = parent;
		processDirectory(directory);
	}

	private boolean isTopLevel() {
		return parent == null;
	}

	private void processDirectory(File directory) throws IOException,
			ParseException, InvalidElementException {

		if (!directory.isDirectory()
				|| (!isTopLevel() && !isPythonPackage(directory)))
			throw new InvalidElementException("Not a package", directory);

		for (File f : directory.listFiles()) {
			try {
				if (f.isDirectory()) {
					Package p = new Package(f, this);
					packages.put(p.getName(), p);
				} else if (f.isFile()) {
					Module m = new Module(f, this);
					modules.put(m.getName(), m);
				}
			} catch (InvalidElementException e) { /* carry on */
			}
		}

		this.name = (isTopLevel()) ? "" : directory.getName();
	}

	/**
	 * Is the given directory a Python package? Does it contain an __init__.py?
	 */
	private boolean isPythonPackage(File directory) {
		if (!directory.isDirectory())
			return false;

		for (File f : directory.listFiles()) {
			String name = ModuleParser.moduleNameFromFile(f);
			if (name != null && name.equals(PACKAGE_TAG_NAME))
				return true;
		}

		return false;
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
	
	public Package getPackage() {
		return parent;
	}

	public Scope getParentScope() {
		return getPackage();
	}
}
