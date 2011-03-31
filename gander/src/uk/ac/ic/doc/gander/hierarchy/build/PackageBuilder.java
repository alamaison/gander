package uk.ac.ic.doc.gander.hierarchy.build;

import java.io.File;

import uk.ac.ic.doc.gander.hierarchy.InvalidElementException;
import uk.ac.ic.doc.gander.hierarchy.Package;

public class PackageBuilder extends Builder {

	private static final String PACKAGE_TAG_NAME = "__init__";
	private static final String PACKAGE_TAG_FILENAME = PACKAGE_TAG_NAME + ".py";

	private BuildablePackage pkg;

	public PackageBuilder(Iterable<File> topLevelDirectories) {

		pkg = new BuildablePackage("", null, null);

		for (File directory : topLevelDirectories) {
			// Some directories on the Python path may not exist. Skip them.
			if (!directory.isDirectory())
				continue;

			processDirectory(directory);
		}
	}

	public Package getPackage() {
		return pkg;
	}

	private PackageBuilder(File directory, Package parent)
			throws InvalidElementException {
		assert parent != null;

		if (!directory.isDirectory())
			throw new InvalidElementException("Not a package", directory);

		File initFile = findPackageInitFile(directory);
		if (initFile == null)
			throw new InvalidElementException("Not a package", directory);

		pkg = new BuildablePackage(directory.getName(), initFile, parent);

		processDirectory(directory);
	}

	private void processDirectory(File directory) {

		for (File f : directory.listFiles()) {
			try {
				if (f.isDirectory()) {
					PackageBuilder builder = new PackageBuilder(f, pkg);
					pkg.addPackage(builder.getPackage());
				} else if (f.isFile()) {
					if (f.getName().equals(PACKAGE_TAG_FILENAME))
						continue;
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
		return findPackageInitFile(directory) != null;
	}

	/**
	 * Find Python package indicator file, __init__.py.
	 */
	private static File findPackageInitFile(File directory) {
		if (!directory.isDirectory())
			return null;

		for (File f : directory.listFiles()) {
			String name = moduleNameFromFile(f);
			if (name != null && name.equals(PACKAGE_TAG_NAME))
				return f;
		}

		return null;
	}
}
