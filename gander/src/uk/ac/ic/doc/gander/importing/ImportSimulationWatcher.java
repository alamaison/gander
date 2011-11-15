package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.model.Namespace;

public interface ImportSimulationWatcher {

	/**
	 * The import mechanism is binding an object to a name in the given
	 * namespace.
	 * 
	 * XXX: This API limits imported objects to Namespaces when it could be
	 * anything.
	 * 
	 * FIXME: importReceiver may not actually be the import receiver. It depends
	 * on the binding scope of 'as' in importReceiver. It could be the global
	 * scope
	 */
	void bindingName(Namespace importReceiver, Namespace loadedObject, String as);
}
