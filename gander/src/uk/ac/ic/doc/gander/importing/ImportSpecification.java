package uk.ac.ic.doc.gander.importing;

public interface ImportSpecification {

	/**
	 * Returns the path whose children are the objects that are bound to names
	 * in the binding namespace of the scope in which this import appears.
	 * 
	 * <ul>
	 * <li>For {@code import a.b.c} that is the empty path</li>
	 * 
	 * <li>For {@code import a.b.c as p} that is {@code a.b}</li>
	 * 
	 * <li>For {@code from a.b.c import d} that is {@code a.b.c}</li>
	 * 
	 * <li>For {@code from a.b.c import d as p} that is {@code a.b.c}</li>
	 * 
	 * <li>For {@code from a.b.c import *} that is {@code a.b.c}</li>
	 * </ul>
	 */
	ImportPath boundObjectParentPath();

	/**
	 * Returns the part of the import module.
	 * 
	 * In a from-style import this means the module with respect to which items
	 * are imported. Note that this doesn't mean the item might not also be a
	 * module. It just means that thre returned path is the only part that can
	 * be relied to to <em>definitely</em> be a module.
	 * 
	 * In a standard import, it is simply the module that is loaded. This is not
	 * necessarily the module that is bound locally.
	 * 
	 * <ul>
	 * <li>For {@code import a.b.c} that is {@code a.b.c}</li>
	 * 
	 * <li>For {@code import a.b.c as p} that is {@code a.b.c}</li>
	 * 
	 * <li>For {@code from a.b.c import d} that is {@code a.b.c}</li>
	 * 
	 * <li>For {@code from a.b.c import d as p} that is {@code a.b.c}</li>
	 * 
	 * <li>For {@code from a.b.c import *} that is {@code a.b.c}</li>
	 * </ul>
	 */
	ImportPath modulePath();

	/**
	 * Returns whether the import is of a kind that only permits modules to be
	 * imported.
	 * 
	 * This is true for regular imports and false for from-style imports.
	 */
	boolean importsAreLimitedToModules();

	/**
	 * Returns an object implementing the binding behaviour of this kind of
	 * import statement.
	 */
	<O, A, C, M> BindingScheme<M> newBindingScheme(
			Import<O, C, M> importInstance,
			ImportSimulator.Binder<O, A, C, M> bindingHandler,
			ImportSimulator.Loader<O, A, M> loader);
}