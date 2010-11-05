package uk.ac.ic.doc.cfg.model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.python.pydev.parser.jython.ParseException;

public class Package implements IModelElement {

	private HashMap<String, Module> modules = new HashMap<String, Module>();
	private HashMap<String, Package> packages = new HashMap<String, Package>();
	private String name;

	private static final String PACKAGE_TAG_NAME = "__init__";

	public Package(File directory) throws IOException, ParseException, InvalidElementException {
		processDirectory(directory, true);
	}

	private Package(File directory, boolean topLevel) throws IOException,
			ParseException, InvalidElementException {
		processDirectory(directory, topLevel);
	}

	private void processDirectory(File directory, boolean topLevel)
			throws IOException, ParseException, InvalidElementException {
		
		if (!directory.isDirectory() || (!topLevel && !isPythonPackage(directory)))
			throw new InvalidElementException("Not a package", directory);

		for (File f : directory.listFiles()) {
			try {
				if (f.isDirectory()) {
					Package p = new Package(f, false);
					packages.put(p.getName(), p);
				} else if (f.isFile()) {
					Module m = new Module(f);
					modules.put(m.getName(), m);
				}
			}
			catch (InvalidElementException e) { /* carry on */ }
		}

		this.name = (topLevel) ? "" : directory.getName();
	}

	/**
	 * Is the given directory a Python package? Does it contain an __init__.py?
	 */
	private boolean isPythonPackage(File directory) {
		if (!directory.isDirectory())
			return false;

		for (File f : directory.listFiles()) {
			String name = Module.moduleNameFromFile(f);
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

	public Map<String, Package> getPackages() {
		return packages;
	}

	public Map<String, Module> getModules() {
		return modules;
	}

}
