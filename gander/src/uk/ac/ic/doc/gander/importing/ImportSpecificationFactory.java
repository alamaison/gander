package uk.ac.ic.doc.gander.importing;

public final class ImportSpecificationFactory {

	public static StandardImportSpecification newImport(String moduleImportName) {
		return StandardImportSpecification.newInstance(moduleImportName);
	}

	public static ImportSpecification newImportAs(String moduleImportName, String alias) {
		return StandardImportAsSpecification.newInstance(moduleImportName, alias);
	}

	public static ImportSpecification newFromImport(String moduleImportName,
			String itemName) {
		return FromImportSpecification.newInstance(moduleImportName, itemName);
	}

	public static ImportSpecification newFromImportAs(String moduleImportName,
			String itemName, String alias) {
		return FromImportAsSpecification.newInstance(moduleImportName, itemName, alias);
	}

	private ImportSpecificationFactory() {
		throw new AssertionError();
	}

}
