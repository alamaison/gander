package uk.ac.ic.doc.gander.model.build;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.python.pydev.parser.jython.ParseException;

import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.ModuleNamespace;
import uk.ac.ic.doc.gander.model.MutableModel;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;

public final class TopLevelModuleLoader {

	public static Module load(MutableModel model) throws ParseException,
			IOException {

		URL builtins = TopLevelModuleLoader.class
				.getResource("dummy_builtins.py");
		File moduleFile;
		try {
			moduleFile = new File(builtins.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException("builtins definitions not found", e);
		}

		FileParser parser = new FileParser(moduleFile);
		ModuleCO codeObject = new ModuleCO("", parser.getAst());
		Module module = new ModuleNamespace(codeObject, null, model, true);
		codeObject.setNamespace(module);

		return module;
	}

	private TopLevelModuleLoader() {
		throw new AssertionError();
	}
}
