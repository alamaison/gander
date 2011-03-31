package uk.ac.ic.doc.gander.hierarchy;

import java.io.File;

public class Module {

	String name;
	File file;
	Package parent;

	public Module(String name, File file, Package parent) {
		assert file != null;
		assert file.isFile();
		assert parent != null;

		this.name = name;
		this.file = file;
		this.parent = parent;
	}

	public Package getParentPackage() {
		return parent;
	}

	public String getName() {
		return name;
	}

	public String getFullyQualifiedName() {
		String parentName = parent.getFullyQualifiedName();
		if ("".equals(parentName))
			return getName();
		else
			return parentName + "." + getName();
	}

	public File getFile() {
		return file;
	}

}