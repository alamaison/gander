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
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Member;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;

public final class ImportTyper implements
		DefaultImportSimulator.ImportEvents<Member, Namespace, Module> {

	private final Model model;
	private final ImportTypeEvent eventHandler;

	public interface ImportTypeEvent {
		void onImportTyped(Namespace scope, String name, Type type);
	}

	public ImportTyper(Model model, ImportTypeEvent eventHandler) {
		this.eventHandler = eventHandler;
		this.model = model;
	}

	public final void bindName(Member loaded, String as,
			Namespace importLocation) {
		assert loaded != null;
		assert importLocation != null;
		assert !as.isEmpty();

		Type type = null;
		if (loaded instanceof Module)
			type = new TModule((Module) loaded);
		else if (loaded instanceof Class)
			type = new TClass((Class) loaded);
		else if (loaded instanceof Function)
			type = new TFunction((Function) loaded);

		// TODO: The target of the 'from foo import bar' can
		// be a variable.

		eventHandler.onImportTyped(importLocation, as, type);
	}

	public final Module loadModule(List<String> importPath,
			Module relativeToModule) {
		List<String> name = new ArrayList<String>(DottedName
				.toImportTokens(relativeToModule.getFullName()));
		name.addAll(importPath);

		// The imported module/package will always exist in the model
		// already if it exists (on disk) at all as the model must have
		// tried to import it already. Therefore we only do a lookup here
		// rather than attempting a load.
		return loadModule(name);
	}

	public Module loadModule(List<String> importPath) {
		return model.lookup(importPath);
	}

	public final void onUnresolvedImport(List<String> importPath,
			Module relativeTo, String as, Namespace codeBlock) {
		System.err.print("WARNING: unresolved import ");
		if (codeBlock != null)
			System.err.print("in " + codeBlock.getFullName() + " ");

		System.err.println("'import " + DottedName.toDottedName(importPath)
				+ "'");

		/*
		 * Can be null if the import is just the middle part of a multi-dotted
		 * import. This loads the middle modules but doesn't bind them.
		 */
		if (codeBlock != null)
			eventHandler.onImportTyped(codeBlock, as, new TUnresolvedImport(
					importPath, relativeTo));
	}

	public final void onUnresolvedImportFromItem(List<String> fromPath,
			Module relativeTo, String itemName, String as, Namespace codeBlock) {
		System.err.print("WARNING: unresolved import ");
		if (codeBlock != null)
			System.err.print("in " + codeBlock.getFullName() + " ");

		System.err.println("'from " + DottedName.toDottedName(fromPath)
				+ " import " + itemName + "': '" + itemName + "' not found");

		// XXX: This isn't really correct. Unlike the other two cases, we
		// don't actually know that the missing item is a module or a
		// package. It _could_ be but equally it could be a class, function
		// or even a variable.
		if (codeBlock != null)
			eventHandler.onImportTyped(codeBlock, as, new TUnresolvedImport(
					fromPath, itemName, relativeTo));
	}

	public Module parentModule(Namespace importReceiver) {
		if (importReceiver instanceof Module)
			return ((Module) importReceiver).getParent();
		else
			return importReceiver.getGlobalNamespace();
	}

	public Namespace lookupNonModuleMember(String itemName,
			Namespace codeObjectWhoseNamespaceWeAreLoadingFrom) {
		Namespace loaded = codeObjectWhoseNamespaceWeAreLoadingFrom
				.getClasses().get(itemName);
		if (loaded == null)
			loaded = codeObjectWhoseNamespaceWeAreLoadingFrom.getFunctions()
					.get(itemName);
		return loaded;
	}
}