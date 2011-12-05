package uk.ac.ic.doc.gander.importing;

final class StandardImportInfo implements ImportInfo {

	static StandardImportInfo newInstance(String moduleImportName) {
		return new StandardImportInfo(LocallyBoundImportNameResolver
				.resolveImport(moduleImportName),
				LocallyBoundImportObjectResolver
						.resolveImport(moduleImportName));
	}

	static StandardImportInfo newInstance(String moduleImportName, String alias) {
		return new StandardImportInfo(LocallyBoundImportNameResolver
				.resolveImportAs(moduleImportName, alias),
				LocallyBoundImportObjectResolver.resolveImportAs(
						moduleImportName, alias));
	}

	private final String bindingName;
	private final String bindingObject;

	public String bindingName() {
		return bindingName;
	}

	public String bindingObject() {
		return bindingObject;
	}

	private StandardImportInfo(String bindingName, String bindingObject) {
		this.bindingName = bindingName;
		this.bindingObject = bindingObject;
	}

}
