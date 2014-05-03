package uk.ac.ic.doc.gander.hierarchy.build;

import java.io.File;

import uk.ac.ic.doc.gander.hierarchy.InvalidElementException;
import uk.ac.ic.doc.gander.hierarchy.Package;
import uk.ac.ic.doc.gander.hierarchy.SourceFile;

public class PackageBuilder {

	private static final String PACKAGE_TAG_NAME = "__init__";
	private static final String PACKAGE_TAG_FILENAME = PACKAGE_TAG_NAME + ".py";

	private BuildablePackage pkg;

	public PackageBuilder(Iterable<File> topLevelDirectories,
			Iterable<File> topLevelSystemDirectories) {

		// Top level package is a system package
		pkg = new BuildablePackage("", null, null, true);

		for (File directory : topLevelDirectories) {
			// Some directories on the Python path may not exist. Skip them.
			if (!directory.isDirectory())
				continue;

			processDirectory(directory, false);
		}

		for (File directory : topLevelSystemDirectories) {
			// Some directories on the Python path may not exist. Skip them.
			if (!directory.isDirectory())
				continue;

			processDirectory(directory, true);
		}
	}

	public Package getPackage() {
		return pkg;
	}

	private PackageBuilder(File directory, Package parent, boolean isSystem)
			throws InvalidElementException {
		assert parent != null;

		if (!directory.isDirectory())
			throw new InvalidElementException("Not a package", directory);

		File initFile = findPackageInitFile(directory);
		if (initFile == null)
			throw new InvalidElementException("Not a package", directory);

		pkg = new BuildablePackage(directory.getName(), initFile, parent,
				isSystem);

		processDirectory(directory, isSystem);
	}

	private void processDirectory(File directory, boolean isSystem) {

		for (File f : directory.listFiles()) {
			try {
				// Don't process a module/package that already exists in the
				// hierarchy. This ensures that modules/packages appearing
				// earlier in the Python path take precedence over those
				// appearing later.
				if (f.isDirectory()) {
					// XXX: Hack assumes packages are indexed by directory name.
					// Even if that assumption is ok, we now have name
					// extraction in two places - here and in the package about
					// to be created.
					if (pkg.getPackages().containsKey(f.getName()))
						continue;

					PackageBuilder builder = new PackageBuilder(f, pkg,
							isSystem);
					pkg.addPackage(builder.getPackage());
				} else if (f.isFile()) {
					if (f.getName().equals(PACKAGE_TAG_FILENAME))
						continue;
					if (pkg.getSourceFiles().containsKey(f.getName()))
						continue;

					pkg.addModule(SourceFile.buildFromSourceFile(f, pkg, isSystem));
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
			String name = BuilderUtils.moduleNameFromFile(f);
			if (name != null && name.equals(PACKAGE_TAG_NAME))
				return f;
		}

		return null;
	}
}
