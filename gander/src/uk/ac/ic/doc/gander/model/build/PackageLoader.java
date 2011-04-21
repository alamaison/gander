package uk.ac.ic.doc.gander.model.build;

import java.io.IOException;

import org.python.pydev.parser.jython.ParseException;

import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Package;

/**
 * Create new Package object in the model by parsing the {@code __init__.py}
 * file and following any import statements.
 * 
 * Much of the point of this class is to translate between a hierarchy package
 * and a runtime-model package.
 */
public final class PackageLoader {

	private Package pkg;

	/**
	 * Load a runtime-model package given a hierarchy package.
	 */
	public PackageLoader(
			uk.ac.ic.doc.gander.hierarchy.Package hierarchyPackage,
			Package parent, Model model) throws ParseException, IOException {
		assert parent != null;

		// Parse __init__ file first so that parse errors abort loading
		// immediately and don't leave empty module in the model
		FileParser parser = new FileParser(hierarchyPackage.getInitFile());

		// This only loads the __init__ file. Submodules of the package aren't
		// <b>loaded</b> when the package is loaded. They must be explicitly
		// mentioned in the import statement.

		pkg = new Package(parser.getAst(), hierarchyPackage.getName(), parent,
				hierarchyPackage.isSystem());

		// Must add the package to the model before we load it in case the
		// __init__ file (about to be loaded) imports modules which try to
		// import this package again. In other words, we have to do this to
		// avoid infinite recursion when there are import cycles.
		parent.addPackage(pkg);

		// XXX: If loading __init__ fails (due to problems with imported
		// modules, most likely), we're left with this empty package in the
		// model. Do we need to clean this up?
		new ImportAwareLoadablePopulator(pkg, model).build(parser.getAst());
	}

	public Package getPackage() {
		return pkg;
	}
}
