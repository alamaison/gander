package uk.ac.ic.doc.gander.hierarchy;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public abstract class Package {

	private String name;
	private File initFile;
	private Package parent;

	public Package(String name, File initFile, Package parent) {
		assert initFile == null || initFile.isFile();
		assert parent == null || initFile != null;

		this.name = name;
		this.initFile = initFile;
		this.parent = parent;
	}

	public abstract Map<String, Module> getModules();

	public abstract Map<String, Package> getPackages();

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
			if ("".equals(parentName))
				return getName();
			else
				return parentName + "." + getName();
		}
	}

	public Package getParentPackage() {
		return parent;
	}

	public File getInitFile() {
		return initFile;
	}

	public Module lookupModule(String relativeName) {
		return lookupModule(dottedNameToImportTokens(relativeName));
	}

	public Package lookupPackage(String relativeName) {
		return lookupPackage(dottedNameToImportTokens(relativeName));
	}

	public Module lookupModule(List<String> relativeName) {
		Queue<String> tokens = new LinkedList<String>(relativeName);

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

	private static List<String> dottedNameToImportTokens(String dottedName) {
		if ("".equals(dottedName))
			return Collections.emptyList();
		return Arrays.asList(dottedName.split("\\."));
	}

}
