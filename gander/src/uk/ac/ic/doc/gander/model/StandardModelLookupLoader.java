package uk.ac.ic.doc.gander.model;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.importing.DefaultImportSimulator.Loader;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;
import uk.ac.ic.doc.gander.model.codeobject.NestedCodeObject;

/**
 * 'Loader' for import simulation that just looks the objects up in a preloaded
 * model.
 */
public final class StandardModelLookupLoader implements
		Loader<CodeObject, CodeObject, ModuleCO> {

	private final Model model;

	public StandardModelLookupLoader(Model model) {
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

	public Module parentModule(Namespace importReceiver) {
		if (importReceiver instanceof Module)
			return ((Module) importReceiver).getParent();
		else
			return importReceiver.getGlobalNamespace();
	}

	public CodeObject loadNonModuleMember(String itemName,
			CodeObject codeObjectWhoseNamespaceWeAreLoadingFrom) {
		Namespace loaded = codeObjectWhoseNamespaceWeAreLoadingFrom
				.oldStyleConflatedNamespace().getClasses().get(itemName);

		if (loaded == null) {
			loaded = codeObjectWhoseNamespaceWeAreLoadingFrom
					.oldStyleConflatedNamespace().getFunctions().get(itemName);
		}

		if (loaded != null) {
			return loaded.codeObject();
		} else {
			return null;
		}
	}

	public ModuleCO parentModule(CodeObject importReceiver) {
		if (importReceiver instanceof NestedCodeObject) {
			return ((NestedCodeObject) importReceiver).enclosingModule();
		} else {
			Module parent = (Module) importReceiver
					.oldStyleConflatedNamespace().getParentScope();
			if (parent != null) {
				return (ModuleCO) parent.codeObject();
			} else {
				return null;
			}
		}
	}
}
