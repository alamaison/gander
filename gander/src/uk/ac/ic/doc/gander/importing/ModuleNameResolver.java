package uk.ac.ic.doc.gander.importing;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;

/**
 * Turns import names into modules.
 * 
 * Import statements of both regular and 'from' kinds, reference a module by
 * name. This name is resolved relative to the module the import statement
 * appears in. This class is responsible for carrying out this resolution in the
 * context of our model.
 */
public class ModuleNameResolver {

	/**
	 * Returns the loaded module corresponding to the given import name.
	 * 
	 * @param relativeImportPath
	 *            the, possibly dotted, import name indicating the module being
	 *            imported.
	 * @param relativeTo
	 *            the module the import is relative to; i.e. the module
	 *            containing the import statement
	 * @param model
	 *            runtime model into which the module being resolved must
	 *            already have been imported
	 * @return the resolved {@link Module}
	 */
	public static Module resolveModule(String relativeImportPath,
			Module relativeTo, Model model) {

		return model.lookup(absoluteModuleImportTokens(relativeImportPath,
				relativeTo));
	}

	private ModuleNameResolver() {
	}

	// Actually works fine for non-modules in an import path too.
	private static List<String> absoluteModuleImportTokens(
			String relativeImportPath, Module relativeTo) {
		List<String> relativeImportTokens = DottedName
				.toImportTokens(relativeImportPath);

		List<String> absoluteImportTokens = new ArrayList<String>(DottedName
				.toImportTokens(relativeTo.getFullName()));
		absoluteImportTokens.addAll(relativeImportTokens);
		return absoluteImportTokens;
	}
}
