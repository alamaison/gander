package uk.ac.ic.doc.gander.model.build;

import java.io.File;

import uk.ac.ic.doc.gander.model.InvalidElementException;
import uk.ac.ic.doc.gander.model.Package;

public class PackageBuilder {

	private static final String PACKAGE_TAG_NAME = "__init__";

	private Package pkg;
	private boolean isTopLevel;

	public PackageBuilder(File directory, Package parent) throws Exception {
		isTopLevel = parent == null;

		if (!directory.isDirectory()
				|| (!isTopLevel && !isPythonPackage(directory)))
			throw new InvalidElementException("Not a package", directory);

		String name = (isTopLevel) ? "" : directory.getName();
		pkg = new Package(name, parent);

		processDirectory(directory);
	}

	public Package getPackage() {
		return pkg;
	}

	private void processDirectory(File directory) throws Exception {

		for (File f : directory.listFiles()) {
			try {
				if (f.isDirectory()) {
					PackageBuilder builder = new PackageBuilder(f, pkg);
					pkg.addPackage(builder.getPackage());
				} else if (f.isFile()) {
					ModuleBuilder builder = new ModuleBuilder(f, pkg);
					pkg.addModule(builder.getModule());
				}
			} catch (InvalidElementException e) {
				/* carry on */
			}
		}
	}

	/**
	 * Is the given directory a Python package? Does it contain an __init__.py?
	 */
	private static boolean isPythonPackage(File directory) {
		if (!directory.isDirectory())
			return false;

		for (File f : directory.listFiles()) {
			String name = ModuleParser.moduleNameFromFile(f);
			if (name != null && name.equals(PACKAGE_TAG_NAME))
				return true;
		}

		return false;
	}
}
