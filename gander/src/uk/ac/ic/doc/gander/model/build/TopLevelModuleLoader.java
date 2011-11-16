package uk.ac.ic.doc.gander.model.build;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.python.pydev.parser.jython.ParseException;

import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.MutableModel;

public final class TopLevelModuleLoader {

	public static void load(Module topLevelModule, MutableModel model)
			throws ParseException, IOException {
		assert topLevelModule != null;

		URL builtins = TopLevelModuleLoader.class
				.getResource("dummy_builtins.py");
		File moduleFile;
		try {
			moduleFile = new File(builtins.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException("builtins definitions not found", e);
		}

		FileParser parser = new FileParser(moduleFile);

		new ImportAwareModulePopulator(topLevelModule, model).build(parser
				.getAst());
		topLevelModule.setAst(parser.getAst());
	}

	private TopLevelModuleLoader() {
		throw new AssertionError();
	}
}
