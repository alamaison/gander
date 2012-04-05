package uk.ac.ic.doc.gander.model;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.flowinference.Namespace;
import uk.ac.ic.doc.gander.importing.ImportPath;
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
		Loader<NamespaceName, Namespace, ModuleCO> {

	private final Model model;

	public NamespaceNameLoader(Model model) {
		this.model = model;
	}

	@Override
	public final ModuleCO loadModule(List<String> importPath,
			ModuleCO relativeToModule) {

		List<String> name = new ArrayList<String>(
				DottedName.toImportTokens(relativeToModule
						.oldStyleConflatedNamespace().getFullName()));

		boolean skippedInitialEmpty = false;
		for (String token : importPath) {

			if (token.isEmpty()) {
				/*
				 * Empty token signify an explicit relative path and every empty
				 * segment means the import climbs one level in the hierarchy.
				 * So we delete parts of the relative name.
				 */
				if (skippedInitialEmpty) {
					if (name.isEmpty()) {
						throw new IllegalArgumentException(
								"Relative segments cannot take import "
										+ "above top level");
					} else {
						name.remove(name.size() - 1);
					}
				} else {
					skippedInitialEmpty = true;
				}

			} else {
				name.add(token);
			}
		}

		// The imported module/package will always exist in the model
		// already if it exists (on disk) at all as the model must have
		// tried to import it already. Therefore we only do a lookup here
		// rather than attempting a load.
		return loadModule(name);
	}

	@Override
	public ModuleCO loadModule(List<String> importPath) {
		return model.lookup(ImportPath.fromTokens(importPath));
	}

	@Override
	public NamespaceName loadModuleNamespaceMember(String itemName,
			ModuleCO sourceModule) {

		return new NamespaceName(itemName,
				sourceModule.fullyQualifiedNamespace());
	}

	@Override
	public Namespace loadAllMembersInModuleNamespace(ModuleCO sourceModule) {
		return sourceModule.fullyQualifiedNamespace();
	}
}
