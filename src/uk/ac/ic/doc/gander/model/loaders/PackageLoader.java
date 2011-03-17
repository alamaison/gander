package uk.ac.ic.doc.gander.model.loaders;

import java.io.File;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Package;
import uk.ac.ic.doc.gander.model.build.BuildablePackage;
import uk.ac.ic.doc.gander.model.build.ModuleParser;
import uk.ac.ic.doc.gander.model.build.PackageModuleBuilder;

public class PackageLoader {

	private BuildablePackage pkg;
	private Model model;

	public Package getPackage() {
		return pkg;
	}

	public PackageLoader(uk.ac.ic.doc.gander.hierarchy.Package hierarchy,
			BuildablePackage parent, Model model) throws Exception {

		assert parent != null;

		this.model = model;

		pkg = new BuildablePackage(hierarchy.getName(), parent);
		parent.addPackage(pkg);

		copyModuleContents(parseInitFile(hierarchy.getName(), parent, hierarchy
				.getInitFile()));
	}

	private void copyModuleContents(Module module) {
		for (Class klass : module.getClasses().values())
			pkg.addClass(klass);
		for (Function function : module.getFunctions().values())
			pkg.addFunction(function);
	}

	private Module parseInitFile(String name, BuildablePackage parent,
			File moduleFile) throws Exception {
		ModuleParser parser = new ModuleParser(moduleFile);

		PackageModuleBuilder builder = new PackageModuleBuilder(name, model,
				null);
		parser.getAst().accept(builder);
		return builder.getModule();
	}
}
