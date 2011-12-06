package uk.ac.ic.doc.gander.flowinference;

import java.util.List;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.TUnresolvedImport;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.importing.DefaultImportSimulator;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.FunctionCO;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;

/**
 * Watches an import simulation for objects being bound to names and creates the
 * correct {@link Type} for the object.
 */
final class ImportedNameTypeWatcher implements
		DefaultImportSimulator.Binder<CodeObject, CodeObject, ModuleCO> {

	private final ImportTypeEvent eventHandler;

	interface ImportTypeEvent {
		void onImportTyped(CodeObject location, String name, Type type);
	}

	ImportedNameTypeWatcher(ImportTypeEvent eventHandler) {
		this.eventHandler = eventHandler;
	}

	public final void bindName(CodeObject loaded, String as,
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
}