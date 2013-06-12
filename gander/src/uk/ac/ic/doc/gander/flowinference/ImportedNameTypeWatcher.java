package uk.ac.ic.doc.gander.flowinference;

import java.util.Map.Entry;

import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyClass;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyFunction;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyModule;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyUnresolvedImport;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.importing.Import;
import uk.ac.ic.doc.gander.importing.ImportSimulator;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.OldNamespace;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;

/**
 * Watches an import simulation for objects being bound to names and creates the
 * correct {@link PyObject} for the object.
 */
final class ImportedNameTypeWatcher implements
		ImportSimulator.Binder<NamespaceName, Namespace, CodeObject, ModuleCO> {

	private final ImportTypeEvent eventHandler;

	interface ImportTypeEvent {
		void onImportTyped(CodeObject location, String name, PyObject type);
	}

	ImportedNameTypeWatcher(ImportTypeEvent eventHandler) {
		this.eventHandler = eventHandler;
	}

	@Override
	public void bindModuleToLocalName(ModuleCO loadedModule, String name,
			CodeObject importReceiver) {
		assert loadedModule != null;
		assert importReceiver != null;
		assert !name.isEmpty();

		eventHandler.onImportTyped(importReceiver, name, new PyModule(
				loadedModule));
	}

	@Override
	public void bindModuleToName(ModuleCO loadedModule, String name,
			ModuleCO container) {
		assert loadedModule != null;
		assert container != null;
		assert !name.isEmpty();

		eventHandler.onImportTyped(container, name, new PyModule(loadedModule));
	}

	@Override
	public void bindObjectToLocalName(NamespaceName importedObject,
			String name, CodeObject container) {

		assert importedObject != null;
		assert container != null;
		assert !name.isEmpty();

		PyObject type = oldSchoolNamespaceLookup(importedObject);
		if (type == null) {
			type = new PyUnresolvedImport(null);
		}

		// TODO: The target of the 'from foo import bar' can
		// be a variable.

		// FIXME: container not necessarily the location the name is bound
		eventHandler.onImportTyped(container, name, type);
	}

	@Override
	public void bindObjectToName(NamespaceName importedObject, String name,
			ModuleCO receivingModule) {
		assert importedObject != null;
		assert receivingModule != null;
		assert !name.isEmpty();

		PyObject type = oldSchoolNamespaceLookup(importedObject);
		if (type == null) {
			type = new PyUnresolvedImport(null);
		}

		// TODO: The target of the 'from foo import bar' can
		// be a variable.

		// FIXME: container not necessarily the location the name is bound
		eventHandler.onImportTyped(receivingModule, name, type);
	}

	@Override
	public void bindAllNamespaceMembers(Namespace sourceNamespace,
			CodeObject container) {

		// FIXME: container not necessarily the location the name is bound

		if (sourceNamespace instanceof OldNamespace) {

			for (Entry<String, Class> importedClass : ((OldNamespace) sourceNamespace)
					.getClasses().entrySet()) {

				eventHandler.onImportTyped(container, importedClass.getKey(),
						new PyClass(importedClass.getValue().codeObject()));
			}

			for (Entry<String, Function> importedFunction : ((OldNamespace) sourceNamespace)
					.getFunctions().entrySet()) {
				eventHandler.onImportTyped(container,
						importedFunction.getKey(), new PyFunction(
								importedFunction.getValue().codeObject()));
			}
		}
	}

	private PyObject oldSchoolNamespaceLookup(NamespaceName importedObjectName) {

		if (importedObjectName.namespace() instanceof OldNamespace) {

			OldNamespace parent = (OldNamespace) importedObjectName.namespace();
			String objectName = importedObjectName.name();

			Module importedModule = parent.getModules().get(objectName);
			if (importedModule != null) {
				return new PyModule(importedModule.codeObject());
			} else {

				Class importedClass = parent.getClasses().get(objectName);

				if (importedClass != null) {
					return new PyClass(importedClass.codeObject());
				} else {

					Function importedFunction = parent.getFunctions().get(
							objectName);
					if (importedFunction != null) {
						return new PyFunction(importedFunction.codeObject());
					} else {
						return null;
					}
				}
			}
		} else {
			return null;
		}
	}

	@Override
	public void onUnresolvedImport(Import<CodeObject, ModuleCO> importInstance,
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
			eventHandler.onImportTyped(relativeTo, name, new PyUnresolvedImport(
					importInstance));
		}
	}

	@Override
	public void onUnresolvedLocalImport(
			Import<CodeObject, ModuleCO> importInstance, String name) {
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
					new PyUnresolvedImport(importInstance));
		}
	}
}