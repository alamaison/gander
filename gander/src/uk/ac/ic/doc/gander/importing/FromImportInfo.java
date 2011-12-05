package uk.ac.ic.doc.gander.importing;

final class FromImportInfo implements ImportInfo {

	static FromImportInfo newInstance(String moduleImportName, String itemName) {
		return new FromImportInfo(LocallyBoundImportNameResolver
				.resolveFromImport(moduleImportName, itemName),
				LocallyBoundImportObjectResolver.resolveFromImport(
						moduleImportName, itemName));
	}

	static FromImportInfo newInstance(String moduleImportName, String itemName,
			String alias) {
		return new FromImportInfo(LocallyBoundImportNameResolver
				.resolveFromImportAs(moduleImportName, itemName, alias),
				LocallyBoundImportObjectResolver.resolveFromImportAs(
						moduleImportName, itemName, alias));
	}

	private final String bindingName;
	private final String bindingObject;

	public String bindingName() {
		return bindingName;
	}

	public String bindingObject() {
		return bindingObject;
	}

	private FromImportInfo(String bindingName, String bindingObject) {
		this.bindingName = bindingName;
		this.bindingObject = bindingObject;
	}

}
