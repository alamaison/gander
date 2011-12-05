package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.model.Member;
import uk.ac.ic.doc.gander.model.Namespace;

public interface ImportSimulationWatcher {

	/**
	 * The import mechanism is binding an object to a name in the given
	 * namespace.
	 * 
	 * FIXME: importLocation may not actually be the import receiver. It depends
	 * on the binding scope of 'as' in importLocation. It could be the global
	 * scope
	 */
	void bindingName(Namespace importLocation, Member loadedObject, String as);
}
