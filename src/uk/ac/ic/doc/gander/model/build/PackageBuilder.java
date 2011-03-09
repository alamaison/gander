package uk.ac.ic.doc.gander.model.build;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.python.pydev.parser.jython.ParseException;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Package;
import uk.ac.ic.doc.gander.model.*;

public class PackageBuilder {

	private static final String PACKAGE_TAG_NAME = "__init__";
	private static final String PACKAGE_TAG_FILENAME = PACKAGE_TAG_NAME + ".py";

	private BuildablePackage pkg;
	private boolean isTopLevel;

	public PackageBuilder(File directory, Package parent, Model model)
			throws Exception {
		isTopLevel = parent == null;

		if (!directory.isDirectory()
				|| (!isTopLevel && !isPythonPackage(directory)))
			throw new InvalidElementException("Not a package", directory);

		String name = (isTopLevel) ? "" : directory.getName();
		pkg = new BuildablePackage(name, parent);

		if (!isTopLevel) {
			buildNormalPackage(directory, parent, name);
		} else {
			buildTopLevelPackage(parent, model, name);
		}

		processDirectory(directory, model);
	}

	public Package getPackage() {
		return pkg;
	}

	private void buildTopLevelPackage(Package parent, Model model, String name)
			throws Exception {
		assert "".equals(name);
		assert parent == null;
		assert model.getTopLevelPackage() == null;
		model.setTopLevelPackage(pkg);
		copyModuleContents(createDummyBuiltins());
	}

	private void buildNormalPackage(File directory, Package parent, String name)
			throws Exception {
		copyModuleContents(createInitPy(directory, name, parent));
	}

	private void copyModuleContents(Module module) {
		for (Class klass : module.getClasses().values())
			pkg.addClass(klass);
		for (Function function : module.getFunctions().values())
			pkg.addFunction(function);
	}

	private Module createInitPy(File directory, String name, Package parent)
			throws Exception {
		File initFile = new File(directory, PACKAGE_TAG_FILENAME);
		return parseFile(name, parent, initFile);
	}

	private Module parseFile(String name, Package parent, File moduleFile)
			throws Exception {
		ModuleParser parser = new ModuleParser(moduleFile);

		ModuleBuilderVisitor builder = new ModuleBuilderVisitor(name, parent);
		parser.getAst().accept(builder);
		return builder.getModule();
	}

	private Module createDummyBuiltins() throws IOException, ParseException,
			InvalidElementException, Exception {
		URL builtins = getClass().getResource("dummy_builtins.py");
		return parseFile("__builtins__", null, new File(builtins.toURI()));
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
