package uk.ac.ic.doc.gander.importing;

/**
 * Common interface to the essential nature of the four particular types of
 * import.
 */
public interface StaticImportSpecification extends ImportSpecification {

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
	 * Returns the name of the object that is bound to a name in the binding
	 * namespace of the scope in which this import appears.
	 * 
	 * <ul>
	 * <li>For {@code import a.b.c} that is {@code a}</li>
	 * 
	 * <li>For {@code import a.b.c as p} that is {@code c}</li>
	 * 
	 * <li>For {@code from a.b.c import d} that is {@code d}</li>
	 * 
	 * <li>For {@code from a.b.c import d as p} that is {@code d}</li>
	 * </ul>
	 * 
	 * This name, added to the result of {@code bindsChildrenOf()}, result in
	 * the path to the bound object.
	 */
	String boundObjectName();

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
	ImportPath loadedPath();
}
