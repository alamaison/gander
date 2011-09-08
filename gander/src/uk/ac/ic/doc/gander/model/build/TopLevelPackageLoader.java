package uk.ac.ic.doc.gander.model.build;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.python.pydev.parser.jython.ParseException;

import uk.ac.ic.doc.gander.model.Module;

public class TopLevelPackageLoader {

	private Module pkg;

	public TopLevelPackageLoader() throws ParseException, IOException {

		URL builtins = getClass().getResource("dummy_builtins.py");
		File moduleFile;
		try {
			moduleFile = new File(builtins.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException("builtins definitions not found", e);
		}

		FileParser parser = new FileParser(moduleFile);

		pkg = new Module(parser.getAst(), "", null, true);

		new ModulePopulator(pkg).build(parser.getAst());
	}

	public Module getPackage() {
		return pkg;
	}
}
