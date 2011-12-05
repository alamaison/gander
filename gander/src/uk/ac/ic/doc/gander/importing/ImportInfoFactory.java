package uk.ac.ic.doc.gander.importing;

public final class ImportInfoFactory {

	public static ImportInfo newImport(String moduleImportName) {
		return StandardImportInfo.newInstance(moduleImportName);
	}

	public static ImportInfo newImportAs(String moduleImportName, String alias) {
		return StandardImportInfo.newInstance(moduleImportName, alias);
	}

	public static ImportInfo newFromImport(String moduleImportName,
			String itemName) {
		return FromImportInfo.newInstance(moduleImportName, itemName);
	}

	public static ImportInfo newFromImportAs(String moduleImportName,
			String itemName, String alias) {
		return FromImportInfo.newInstance(moduleImportName, itemName, alias);
	}

	private ImportInfoFactory() {
		throw new AssertionError();
	}

}
