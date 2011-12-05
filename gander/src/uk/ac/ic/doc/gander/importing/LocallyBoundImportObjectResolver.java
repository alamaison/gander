package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.DottedName;

/**
 * Determines the path of the object this is bound to a name in the binding
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
final class LocallyBoundImportObjectResolver {

	static String resolveImport(String importName) {
		return DottedName.toImportTokens(importName).get(0);
	}

	static String resolveImportAs(String importName, String as) {
		return importName;
	}

	static String resolveFromImport(String fromName, String itemName) {
		return fromName + "." + itemName;
	}

	static String resolveFromImportAs(String fromName, String itemName,
			String as) {
		return fromName + "." + itemName;
	}

	private LocallyBoundImportObjectResolver() {
		throw new AssertionError();
	}
}