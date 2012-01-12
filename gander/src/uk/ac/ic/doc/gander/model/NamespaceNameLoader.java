package uk.ac.ic.doc.gander.model;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;

/**
 * 'Loader' for import simulation that just 'loads' a object be associating a
 * name with the namespace it was loaded from.
 * 
 * This is used by flow analysis that only cares where an object came from, not
 * what it is.
 */
public final class NamespaceNameLoader implements
		Loader<NamespaceName, ModuleCO> {

	private final Model model;

	public NamespaceNameLoader(Model model) {
		this.model = model;
	}

	public final ModuleCO loadModule(List<String> importPath,
			ModuleCO relativeToModule) {
		List<String> name = new ArrayList<String>(DottedName
				.toImportTokens(relativeToModule.oldStyleConflatedNamespace()
						.getFullName()));
		name.addAll(importPath);

		// The imported module/package will always exist in the model
		// already if it exists (on disk) at all as the model must have
		// tried to import it already. Therefore we only do a lookup here
		// rather than attempting a load.
		return loadModule(name);
	}

	public ModuleCO loadModule(List<String> importPath) {
		Module module = model.lookup(importPath);
		if (module != null) {
			return module.codeObject();
		} else {
			return null;
		}
	}

	public NamespaceName loadNonModuleMember(String itemName,
			ModuleCO sourceModule) {
		return new NamespaceName(itemName, sourceModule
				.fullyQualifiedNamespace());
	}
}
