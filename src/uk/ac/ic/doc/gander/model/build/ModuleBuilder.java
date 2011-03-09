package uk.ac.ic.doc.gander.model.build;

import java.io.File;

public class ModuleBuilder {

	private uk.ac.ic.doc.gander.model.Module module;

	public ModuleBuilder(File moduleFile, BuildablePackage parent) throws Exception {
		ModuleParser parser = new ModuleParser(moduleFile);
		ModuleBuilderVisitor builder = new ModuleBuilderVisitor(parser
				.getName(), parent);
		parser.getAst().accept(builder);
		module = builder.getModule();
		parent.addModule(module);
	}

	public uk.ac.ic.doc.gander.model.Module getModule() {
		return module;
	}

}
