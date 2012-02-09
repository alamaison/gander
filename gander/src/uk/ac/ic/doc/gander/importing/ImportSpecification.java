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
	 * Returns whether the import is of a kind that only permits modules to be
	 * imported.
	 * 
	 * This is true for regular imports and false for from-style imports.
	 */
	boolean importsAreLimitedToModules();
}