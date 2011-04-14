package uk.ac.ic.doc.gander.model.loaders;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.python.pydev.parser.jython.ParseException;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Package;
import uk.ac.ic.doc.gander.model.build.BuildablePackage;
import uk.ac.ic.doc.gander.model.build.DumbModuleBuilder;
import uk.ac.ic.doc.gander.model.build.ModuleParser;

public class TopLevelPackageLoader {

	private BuildablePackage pkg;

	public TopLevelPackageLoader() throws ParseException, IOException {

		pkg = new BuildablePackage("", null);

		copyModuleContents(createDummyBuiltins());
	}

	public Package getPackage() {
		return pkg;
	}

	private void copyModuleContents(Module module) {
		for (Class klass : module.getClasses().values())
			pkg.addClass(klass);
		for (Function function : module.getFunctions().values())
			pkg.addFunction(function);
	}

	private Module parseBuiltinsFile(String name, File moduleFile)
			throws ParseException, IOException {
		ModuleParser parser = new ModuleParser(moduleFile);

		DumbModuleBuilder builder = new DumbModuleBuilder(name, pkg);
		builder.build(parser.getAst());
		return builder.getModule();
	}

	private Module createDummyBuiltins() throws ParseException, IOException {
		URL builtins = getClass().getResource("dummy_builtins.py");
		try {
			return parseBuiltinsFile("__builtins__", new File(builtins.toURI()));
		} catch (URISyntaxException e) {
			throw new RuntimeException("builtins definitions not found", e);
		}
	}
}
