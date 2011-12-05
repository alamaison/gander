package uk.ac.ic.doc.gander.flowinference;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.TUnresolvedImport;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.importing.DefaultImportSimulator;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.FunctionCO;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;
import uk.ac.ic.doc.gander.model.codeobject.NestedCodeObject;

public final class ImportTyper implements
		DefaultImportSimulator.ImportEvents<Object, CodeObject, ModuleCO> {

	private final Model model;
	private final ImportTypeEvent eventHandler;

	public interface ImportTypeEvent {
		void onImportTyped(CodeObject location, String name, Type type);
	}

	public ImportTyper(Model model, ImportTypeEvent eventHandler) {
		this.eventHandler = eventHandler;
		this.model = model;
	}

	public final void bindName(Object loaded, String as,
			CodeObject importLocation) {
		assert loaded != null;
		assert importLocation != null;
		assert !as.isEmpty();

		Type type = null;
		if (loaded instanceof ModuleCO)
			type = new TModule((ModuleCO) loaded);
		else if (loaded instanceof ClassCO)
			type = new TClass((ClassCO) loaded);
		else if (loaded instanceof FunctionCO)
			type = new TFunction((FunctionCO) loaded);

		// TODO: The target of the 'from foo import bar' can
		// be a variable.

		eventHandler.onImportTyped(importLocation, as, type);
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

	public final void onUnresolvedImport(List<String> importPath,
			ModuleCO relativeTo, String as, CodeObject codeBlock) {
		System.err.print("WARNING: unresolved import ");
		if (codeBlock != null)
			System.err.print("in " + codeBlock.absoluteDescription() + " ");

		System.err.println("'import " + DottedName.toDottedName(importPath)
				+ "'");

		/*
		 * Can be null if the import is just the middle part of a multi-dotted
		 * import. This loads the middle modules but doesn't bind them.
		 */
		if (codeBlock != null)
			eventHandler.onImportTyped(codeBlock, as, new TUnresolvedImport(
					importPath, relativeTo.oldStyleConflatedNamespace()));
	}

	public final void onUnresolvedImportFromItem(List<String> fromPath,
			ModuleCO relativeTo, String itemName, String as,
			CodeObject codeBlock) {
		System.err.print("WARNING: unresolved import ");
		if (codeBlock != null)
			System.err.print("in " + codeBlock.absoluteDescription() + " ");

		System.err.println("'from " + DottedName.toDottedName(fromPath)
				+ " import " + itemName + "': '" + itemName + "' not found");

		// XXX: This isn't really correct. Unlike the other two cases, we
		// don't actually know that the missing item is a module or a
		// package. It _could_ be but equally it could be a class, function
		// or even a variable.
		if (codeBlock != null)
			eventHandler.onImportTyped(codeBlock, as,
					new TUnresolvedImport(fromPath, itemName, relativeTo
							.oldStyleConflatedNamespace()));
	}

	public Module parentModule(Namespace importReceiver) {
		if (importReceiver instanceof Module)
			return ((Module) importReceiver).getParent();
		else
			return importReceiver.getGlobalNamespace();
	}

	public Namespace lookupNonModuleMember(String itemName,
			CodeObject codeObjectWhoseNamespaceWeAreLoadingFrom) {
		Namespace loaded = codeObjectWhoseNamespaceWeAreLoadingFrom
				.oldStyleConflatedNamespace().getClasses().get(itemName);
		if (loaded == null)
			loaded = codeObjectWhoseNamespaceWeAreLoadingFrom
					.oldStyleConflatedNamespace().getFunctions().get(itemName);
		return loaded;
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