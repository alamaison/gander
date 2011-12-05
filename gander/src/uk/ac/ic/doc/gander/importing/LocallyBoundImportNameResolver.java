package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.DottedName;

/**
 * Determines the name that a module is bound to in the binding namespace of the
 * scope in which the import appears.
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
final class LocallyBoundImportNameResolver {

	static String resolveImport(String importName) {
		return DottedName.toImportTokens(importName).get(0);
	}

	static String resolveImportAs(String importName, String as) {
		return as;
	}

	static String resolveFromImport(String fromName, String itemName) {
		return itemName;
	}

	static String resolveFromImportAs(String fromName, String itemName,
			String as) {
		return as;
	}

	private LocallyBoundImportNameResolver() {
		throw new AssertionError();
	}
}