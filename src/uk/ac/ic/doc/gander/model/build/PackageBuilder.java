package uk.ac.ic.doc.gander.model.build;

import java.io.File;

import uk.ac.ic.doc.gander.model.Package;
import uk.ac.ic.doc.gander.model.*;

public class PackageBuilder {

	private static final String PACKAGE_TAG_NAME = "__init__";
	private static final String PACKAGE_TAG_FILENAME = PACKAGE_TAG_NAME + ".py";

	private Package pkg;
	private boolean isTopLevel;

	public PackageBuilder(File directory, Package parent, Model model)
			throws Exception {
		isTopLevel = parent == null;

		if (!directory.isDirectory()
				|| (!isTopLevel && !isPythonPackage(directory)))
			throw new InvalidElementException("Not a package", directory);

		String name = (isTopLevel) ? "" : directory.getName();
		if (!isTopLevel)
			pkg = new Package(name, parent, createInitPy(directory, name,
					parent));
		else
			pkg = new Package(name, parent, null);

		if (isTopLevel)
			model.setTopLevelPackage(pkg);

		processDirectory(directory, model);
	}

	public Package getPackage() {
		return pkg;
	}

	private Module createInitPy(File directory, String name, Package parent)
			throws Exception {

		File initFile = new File(directory, PACKAGE_TAG_FILENAME);
		ModuleParser parser = new ModuleParser(initFile);

		ModuleBuilderVisitor initBuilder = new ModuleBuilderVisitor(name,
				parent);
		parser.getAst().accept(initBuilder);
		return initBuilder.getModule();
	}

	private void processDirectory(File directory, Model model) throws Exception {

		for (File f : directory.listFiles()) {
			try {
				if (f.isDirectory()) {
					PackageBuilder builder = new PackageBuilder(f, pkg, model);
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
