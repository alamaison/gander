package uk.ac.ic.doc.gander.importing;

/**
 * Representation of an import statement at a particular location.
 * 
 * @param <C>
 *            type of object representing the code object in which the import
 *            appears
 * @param <M>
 *            type of object representing modules in the runtime model
 */
public interface Import<C, M> {

	ImportStatement statement();

	M relativeTo();

	C container();
}