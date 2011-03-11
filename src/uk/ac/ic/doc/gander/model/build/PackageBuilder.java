package uk.ac.ic.doc.gander.model.build;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.python.pydev.parser.jython.ParseException;

import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.InvalidElementException;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Package;

public class PackageBuilder {

	private BuildablePackage pkg;

	public PackageBuilder(File directory, Model model) throws Exception {

		Hierarchy hierarchy = new Hierarchy(directory);

		pkg = new BuildablePackage("", null);

		buildTopLevelPackage(model);

		processHierarchyPackage(hierarchy.getTopLevelPackage(), model);
	}

	private PackageBuilder(uk.ac.ic.doc.gander.hierarchy.Package hierarchy,
			Package parent, Model model) throws Exception {

		assert parent != null;

		pkg = new BuildablePackage(hierarchy.getName(), parent);

		copyModuleContents(parseFile(hierarchy.getName(), parent, hierarchy
				.getInitFile()));

		processHierarchyPackage(hierarchy, model);
	}

	public Package getPackage() {
		return pkg;
	}

	private void buildTopLevelPackage(Model model) throws Exception {
		assert model.getTopLevelPackage() == null;
		model.setTopLevelPackage(pkg);
		copyModuleContents(createDummyBuiltins());
		// processPythonPath(model);
	}

	private void copyModuleContents(Module module) {
		for (Class klass : module.getClasses().values())
			pkg.addClass(klass);
		for (Function function : module.getFunctions().values())
			pkg.addFunction(function);
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

	private void processHierarchyPackage(
			uk.ac.ic.doc.gander.hierarchy.Package hierarchy, Model model)
			throws Exception {

		for (uk.ac.ic.doc.gander.hierarchy.Package subPackage : hierarchy
				.getPackages().values()) {

			PackageBuilder builder = new PackageBuilder(subPackage, pkg, model);
			pkg.addPackage(builder.getPackage());
		}

		for (uk.ac.ic.doc.gander.hierarchy.Module subModule : hierarchy
				.getModules().values()) {

			ModuleBuilder builder = new ModuleBuilder(subModule, pkg);
			pkg.addModule(builder.getModule());
		}

	}

	private void processPythonPath(Model model) throws Exception {
		String[] pythonPath = { "/usr/lib/python2.6",
				"/usr/lib/python2.6/plat-linux2", "/usr/lib/python2.6/lib-tk",
				"/usr/lib/python2.6/lib-old", "/usr/lib/python2.6/lib-dynload",
				"/usr/lib/python2.6/dist-packages",
				"/usr/lib/python2.6/dist-packages/PIL",
				"/usr/lib/python2.6/dist-packages/gst-0.10",
				"/usr/lib/pymodules/python2.6",
				"/usr/lib/python2.6/dist-packages/gtk-2.0",
				"/usr/lib/pymodules/python2.6/gtk-2.0",
				"/usr/lib/python2.6/dist-packages/wx-2.8-gtk2-unicode",
				"/usr/local/lib/python2.6/dist-packages", };
		String[] pythonPath2 = { "/usr/lib/python2.6" };
		for (String path : pythonPath2) {
			File directory = new File(path);
			if (directory == null || !directory.isDirectory())
				continue; // directory doesn't exist

			// processDirectory(directory, model);
		}
	}
}
