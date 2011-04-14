package uk.ac.ic.doc.gander.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.python.pydev.parser.jython.ParseException;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.model.build.BuildablePackage;
import uk.ac.ic.doc.gander.model.loaders.ModuleLoader;
import uk.ac.ic.doc.gander.model.loaders.PackageLoader;
import uk.ac.ic.doc.gander.model.loaders.TopLevelPackageLoader;

public class Model {

	private Package topLevelPackage;
	private Hierarchy hierarchy;

	public Model(Hierarchy hierarchy) throws ParseException, IOException {
		this.hierarchy = hierarchy;
		topLevelPackage = new TopLevelPackageLoader().getPackage();
	}

	public Package getTopLevelPackage() {
		return topLevelPackage;
	}

	public Module lookupModule(String importName) {
		return lookupModule(DottedName.toImportTokens(importName));
	}

	public Package lookupPackage(String importName) {
		return lookupPackage(DottedName.toImportTokens(importName));
	}

	public Importable lookup(String importName) {
		List<String> tokens = DottedName.toImportTokens(importName);
		Importable imported = lookupPackage(tokens);
		if (imported == null)
			imported = lookupModule(tokens);
		return imported;
	}

	public Importable load(String importName) throws ParseException,
			IOException {
		List<String> tokens = DottedName.toImportTokens(importName);
		Importable imported = loadPackage(tokens);
		if (imported == null)
			imported = loadModule(tokens);
		return imported;
	}

	public Module lookupModule(List<String> importNameTokens) {
		return getTopLevelPackage().lookupModule(importNameTokens);
	}

	public Package lookupPackage(List<String> importNameTokens) {
		return getTopLevelPackage().lookupPackage(importNameTokens);
	}

	public Module loadModule(String fullyQualifiedName) throws ParseException,
			IOException {
		return loadModule(DottedName.toImportTokens(fullyQualifiedName));
	}

	public Package loadPackage(String fullyQualifiedName)
			throws ParseException, IOException {
		return loadPackage(DottedName.toImportTokens(fullyQualifiedName));
	}

	/**
	 * Load module if it hasn't been loaded before.
	 * 
	 * Will also load any parent packages if they haven't been loaded yet.
	 */
	public Module loadModule(List<String> fullyQualifiedPath)
			throws ParseException, IOException {
		// Loading must be idempotent so if the module is already loaded we
		// must return the same instance
		Module loaded = lookupModule(fullyQualifiedPath);
		if (loaded == null)
			loaded = reallyLoadModule(fullyQualifiedPath);

		return loaded;
	}

	/**
	 * Load module as well as any parent packages that haven't been loaded
	 * already.
	 */
	private Module reallyLoadModule(List<String> fullyQualifiedPath)
			throws ParseException, IOException {
		List<String> path = new ArrayList<String>();

		// Loading a module means also loading any packages above it in the
		// hierarchy so we load the package whose path is made from all but
		// the final dotted segment. Loading this package takes care of
		// loading any of its parents.
		for (int i = 0; i < fullyQualifiedPath.size() - 1; ++i)
			path.add(fullyQualifiedPath.get(i));

		Package parent = loadPackage(path);
		if (parent == null)
			return null;

		// Add module name to the path we used earlier to load the package
		if (fullyQualifiedPath.size() > 0)
			path.add(fullyQualifiedPath.get(fullyQualifiedPath.size() - 1));

		return loadModuleIntoParent(path, parent);
	}

	/**
	 * Load package if it hasn't been loaded before.
	 * 
	 * Will also load any parent packages if they haven't been loaded yet.
	 */
	public Package loadPackage(List<String> fullyQualifiedPath)
			throws ParseException, IOException {
		Queue<String> tokens = new LinkedList<String>(fullyQualifiedPath);
		List<String> loadedPath = new ArrayList<String>();

		Package loaded = topLevelPackage;
		while (loaded != null && !tokens.isEmpty()) {
			loadedPath.add(tokens.remove());

			// Loading must be idempotent so if the package is already loaded we
			// must return the same instance
			Package pkg = lookupPackage(loadedPath);
			if (pkg == null)
				pkg = loadPackageIntoParent(loadedPath, loaded);
			loaded = pkg;
		}

		return loaded;
	}

	private Module loadModuleIntoParent(List<String> fullyQualifiedPath,
			Package parent) throws ParseException, IOException {

		uk.ac.ic.doc.gander.hierarchy.Module pkg = hierarchy
				.findModule(fullyQualifiedPath);
		if (pkg == null)
			return null;
		return new ModuleLoader(pkg, (BuildablePackage) parent, this)
				.getModule();
	}

	private Package loadPackageIntoParent(List<String> fullyQualifiedPath,
			Package parent) throws ParseException, IOException {

		uk.ac.ic.doc.gander.hierarchy.Package pkg = hierarchy
				.findPackage(fullyQualifiedPath);
		if (pkg == null)
			return null;
		return new PackageLoader(pkg, (BuildablePackage) parent, this)
				.getPackage();
	}
}
