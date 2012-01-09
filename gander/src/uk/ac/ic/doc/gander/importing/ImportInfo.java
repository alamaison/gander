package uk.ac.ic.doc.gander.importing;

public interface ImportInfo {

	/**
	 * Returns the name that a module is bound to in the binding namespace of
	 * the scope in which the import appears.
	 * 
	 * <ul>
	 * <li>For {@code import a.b.c} that is {@code "a"}</li>
	 * 
	 * <li>For {@code import a.b.c as p} that is {@code "p"}</li>
	 * 
	 * <li>For {@code from a.b.c import d} that is {@code "d"}</li>
	 * 
	 * <li>For {@code from a.b.c import d as p} that is {@code "p"}</li>
	 * </ul>
	 */
	String bindingName();

	/**
	 * Returns the path of the object that is bound to a name in the binding
	 * namespace of the scope in which the import appears.
	 * 
	 * <ul>
	 * <li>For {@code import a.b.c} that is {@code a}</li>
	 * 
	 * <li>For {@code import a.b.c as p} that is {@code a.b.c}</li>
	 * 
	 * <li>For {@code from a.b.c import d} that is {@code a.b.c.d}</li>
	 * 
	 * <li>For {@code from a.b.c import d as p} that is {@code a.b.c.d}</li>
	 * </ul>
	 */
	String bindingObject();

	/**
	 * Returns the path whose objects are loaded as part of the import process.
	 * 
	 * <ul>
	 * <li>For {@code import a.b.c} that is {@code a.b.c}</li>
	 * 
	 * <li>For {@code import a.b.c as p} that is {@code a.b.c}</li>
	 * 
	 * <li>For {@code from a.b.c import d} that is {@code a.b.c.d}</li>
	 * 
	 * <li>For {@code from a.b.c import d as p} that is {@code a.b.c.d}</li>
	 * </ul>
	 */
	ImportPath objectPath();

	<O, C, M> BindingScheme<M, M> newBindingScheme(
			C outerImportReceiver,
			ImportSimulator.Binder<O, C, M> bindingHandler,
			ImportSimulator.Loader<O, C, M> loader);
}
