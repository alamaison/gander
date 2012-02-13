package uk.ac.ic.doc.gander.importing;

/**
 * Representation of an import statement at a particular location.
 * 
 * @param <O>
 *            the type of Java objects representing general Python objects that
 *            can be imported (including modules and other code objects)
 * @param <C>
 *            type of object representing the code object in which the import
 *            appears
 * @param <M>
 *            type of object representing modules in the runtime model
 */
public interface Import<O, C, M> {

	ImportSpecification specification();

	M relativeTo();

	C container();

	<A> BindingScheme<M> newBindingScheme(
			ImportSimulator.Binder<O, A, C, M> bindingHandler,
			ImportSimulator.Loader<O, A, M> loader);
}