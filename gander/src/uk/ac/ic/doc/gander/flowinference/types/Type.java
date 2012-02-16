package uk.ac.ic.doc.gander.flowinference.types;

import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.Namespace;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;

public interface Type {
	String getName();

	/** Returns the inferred type of the named member of the modelled type. */
	Result<Type> memberType(String memberName, SubgoalManager goalManager);

	/**
	 * Returns the namespaces that may be accessed when reading a member from
	 * this type of object.
	 * 
	 * There may be more than one, for instance accessing a member of an object
	 * instance may read from the object instance's namespace as well as the
	 * namespace of the object's class.
	 */
	Set<Namespace> memberReadableNamespaces();

	/**
	 * Returns the namespace that is modified when storing a value as a member
	 * of this type of object.
	 * 
	 * AFAIK, there can only be one, unlike the situation with reading.
	 * 
	 * TODO: investigate if there is any sitatuation where the target of an
	 * attribute write in Python is not limited to one namespace.
	 */
	Namespace memberWriteableNamespace();
}
