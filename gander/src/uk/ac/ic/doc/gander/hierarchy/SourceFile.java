package uk.ac.ic.doc.gander.hierarchy;

import java.io.File;

import uk.ac.ic.doc.gander.hierarchy.build.BuilderUtils;

public class SourceFile {

	private String name;
	private File file;
	private Package parent;
	private boolean isSystem;

	private SourceFile(String name, File file, Package parent, boolean isSystem) {
		assert file != null;
		assert file.isFile();
		assert parent != null;

		this.name = name;
		this.file = file;
		this.parent = parent;
		this.isSystem = isSystem;
	}

	public static SourceFile buildFromSourceFile(File moduleFile, Package parent,
			boolean isSystem) throws InvalidElementException {

		String name = BuilderUtils.moduleNameFromFile(moduleFile);
		if (name == null)
			throw new InvalidElementException("Not a Python source file",
					moduleFile);

		return new SourceFile(name, moduleFile, parent, isSystem);
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

	public boolean isSystem() {
		return isSystem;
	}

}
