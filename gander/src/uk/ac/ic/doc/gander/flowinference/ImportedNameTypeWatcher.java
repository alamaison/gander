package uk.ac.ic.doc.gander.flowinference;

import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.TUnresolvedImport;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.importing.Import;
import uk.ac.ic.doc.gander.importing.ImportSimulator;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;

/**
 * Watches an import simulation for objects being bound to names and creates the
 * correct {@link Type} for the object.
 */
final class ImportedNameTypeWatcher implements
		ImportSimulator.Binder<NamespaceName, CodeObject, ModuleCO> {

	private final ImportTypeEvent eventHandler;

	interface ImportTypeEvent {
		void onImportTyped(CodeObject location, String name, Type type);
	}

	ImportedNameTypeWatcher(ImportTypeEvent eventHandler) {
		this.eventHandler = eventHandler;
	}

	public void bindModuleToLocalName(ModuleCO loadedModule, String name,
			CodeObject importReceiver) {
		assert loadedModule != null;
		assert importReceiver != null;
		assert !name.isEmpty();

		eventHandler.onImportTyped(importReceiver, name, new TModule(
				loadedModule));
	}

	public void bindModuleToName(ModuleCO loadedModule, String name,
			ModuleCO container) {
		assert loadedModule != null;
		assert container != null;
		assert !name.isEmpty();

		eventHandler.onImportTyped(container, name, new TModule(loadedModule));
	}

	public void bindObjectToLocalName(NamespaceName importedObject,
			String name, CodeObject container) {

		assert importedObject != null;
		assert container != null;
		assert !name.isEmpty();

		Type type = oldSchoolNamespaceLookup(importedObject);
		if (type == null) {
			type = new TUnresolvedImport(null);
		}

		// TODO: The target of the 'from foo import bar' can
		// be a variable.

		eventHandler.onImportTyped(container, name, type);
	}

	public void bindObjectToName(NamespaceName importedObject, String name,
			ModuleCO receivingModule) {
		assert importedObject != null;
		assert receivingModule != null;
		assert !name.isEmpty();

		Type type = oldSchoolNamespaceLookup(importedObject);
		if (type == null) {
			type = new TUnresolvedImport(null);
		}

		// TODO: The target of the 'from foo import bar' can
		// be a variable.

		eventHandler.onImportTyped(receivingModule, name, type);
	}

	private Type oldSchoolNamespaceLookup(NamespaceName importedObjectName) {

		Namespace parent = importedObjectName.namespace();
		String objectName = importedObjectName.name();

		Module importedModule = parent.getModules().get(objectName);
		if (importedModule != null) {
			return new TModule(importedModule.codeObject());
		} else {

			Class importedClass = parent.getClasses().get(objectName);

			if (importedClass != null) {
				return new TClass(importedClass.codeObject());
			} else {

				Function importedFunction = parent.getFunctions().get(
						objectName);
				if (importedFunction != null) {
					return new TFunction(importedFunction.codeObject());
				} else {
					return null;
				}
			}
		}
	}

	public void onUnresolvedImport(
			Import<NamespaceName, CodeObject, ModuleCO> importInstance,
			String name, ModuleCO relativeTo) {
		System.err.print("WARNING: unresolved import at '" + name + "': "
				+ importInstance);

		/*
		 * Can be null if the import is just the middle part of a multi-dotted
		 * import. This loads the middle modules but doesn't bind them.
		 */
		if (relativeTo != null) {
			/*
			 * XXX: We don't actually know that the missing item is a module. It
			 * _could_ be but equally it could be a class, function or even a
			 * variable.
			 */
			eventHandler.onImportTyped(relativeTo, name, new TUnresolvedImport(
					importInstance));
		}
	}

	public void onUnresolvedLocalImport(
			Import<NamespaceName, CodeObject, ModuleCO> importInstance,
			String name) {
		System.err.print("WARNING: unresolved import at '" + name + "': "
				+ importInstance);

		/*
		 * Can be null if the import is just the middle part of a multi-dotted
		 * import. This loads the middle modules but doesn't bind them.
		 */
		if (importInstance.container() != null) {
			/*
			 * XXX: We don't actually know that the missing item is a module. It
			 * _could_ be but equally it could be a class, function or even a
			 * variable.
			 */
			eventHandler.onImportTyped(importInstance.container(), name,
					new TUnresolvedImport(importInstance));
		}
	}
}