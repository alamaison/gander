package uk.ac.ic.doc.gander.model.build;

public class ModuleBuilder {

	private uk.ac.ic.doc.gander.model.Module module;

	public ModuleBuilder(uk.ac.ic.doc.gander.hierarchy.Module hierarchyModule,
			BuildablePackage parent) throws Exception {
		ModuleParser parser = new ModuleParser(hierarchyModule.getFile());
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
