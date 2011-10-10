package uk.ac.ic.doc.gander.flowinference;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.TUnresolvedImport;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.importing.ImportSimulator;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;

public abstract class ImportResolver extends ImportSimulator {

	private final Model model;

	protected abstract void put(Namespace scope, String name, Type type);

	protected ImportResolver(Model model, Namespace importReceiver,
			Module topLevel) {
		super(importReceiver, topLevel);
		this.model = model;
	}

	@Override
	protected final void bindName(Namespace importReceiver, Namespace loaded,
			String as) {
		assert loaded != null;
		assert importReceiver != null;
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

		put(importReceiver, as, type);
	}

	@Override
	protected final Module simulateLoad(List<String> importPath,
			Module relativeToPackage) {
		List<String> name = new ArrayList<String>(DottedName
				.toImportTokens(relativeToPackage.getFullName()));
		name.addAll(importPath);

		// The imported module/package will always exist in the model
		// already if it exists (on disk) at all as the model must have
		// tried to import it already. Therefore we only do a lookup here
		// rather than attempting a load.
		return model.lookup(name);
	}

	@Override
	protected final void onUnresolvedImport(List<String> importPath,
			Module relativeToPackage, Namespace importReceiver, String as) {
		System.err.print("WARNING: unresolved import ");
		if (importReceiver != null)
			System.err.print("in " + importReceiver.getFullName() + " ");

		System.err.println("'import " + DottedName.toDottedName(importPath)
				+ "'");

		put(importReceiver, as, new TUnresolvedImport(importPath,
				relativeToPackage));
	}

	@Override
	protected final void onUnresolvedImportFromItem(List<String> fromPath,
			String itemName, Module relativeToPackage,
			Namespace importReceiver, String as) {
		System.err.print("WARNING: unresolved import ");
		if (importReceiver != null)
			System.err.print("in " + importReceiver.getFullName() + " ");

		System.err
				.println("'from " + DottedName.toDottedName(fromPath)
						+ " import " + itemName + "': '" + itemName
						+ "' not found");

		// XXX: This isn't really correct. Unlike the other two cases, we
		// don't actually know that the missing item is a module or a
		// package. It _could_ be but equally it could be a class, function
		// or even a variable.
		put(importReceiver, as, new TUnresolvedImport(fromPath, itemName,
				relativeToPackage));
	}
}