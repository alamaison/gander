package uk.ac.ic.doc.gander.model.loaders;

import java.io.IOException;

import org.python.pydev.parser.jython.ParseException;

import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.build.BuildablePackage;
import uk.ac.ic.doc.gander.model.build.ModuleBuilder;
import uk.ac.ic.doc.gander.model.build.ModuleParser;

public class ModuleLoader {

	private uk.ac.ic.doc.gander.model.Module module;

	public ModuleLoader(uk.ac.ic.doc.gander.hierarchy.Module hierarchyModule,
			BuildablePackage parent, Model model) throws ParseException,
			IOException {
		ModuleParser parser = new ModuleParser(hierarchyModule.getFile());
		ModuleBuilder builder = new ModuleBuilder(hierarchyModule.getName(),
				model, parent);
		builder.build(parser.getAst());
		module = builder.getModule();
	}

	public uk.ac.ic.doc.gander.model.Module getModule() {
		return module;
	}

}
