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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((bindingName == null) ? 0 : bindingName.hashCode());
		result = prime * result
				+ ((bindingObject == null) ? 0 : bindingObject.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StandardImportInfo other = (StandardImportInfo) obj;
		if (bindingName == null) {
			if (other.bindingName != null)
				return false;
		} else if (!bindingName.equals(other.bindingName))
			return false;
		if (bindingObject == null) {
			if (other.bindingObject != null)
				return false;
		} else if (!bindingObject.equals(other.bindingObject))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StandardImportInfo [import " + bindingObject + " as "
				+ bindingName + "]";
	}
}
