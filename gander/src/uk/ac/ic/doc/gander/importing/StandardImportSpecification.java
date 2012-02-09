package uk.ac.ic.doc.gander.importing;

final class StandardImportSpecification implements StaticImportSpecification {

	private final ImportPath moduleImportPath;

	/**
	 * Creates new standard (non-from) import.
	 * 
	 * @param moduleImportPath
	 *            the relative path of the module being imported
	 */
	static StandardImportSpecification newInstance(String moduleImportPath) {
		return new StandardImportSpecification(
				ImportPath.fromDottedName(moduleImportPath));
	}

	@Override
	public String bindingName() {
		return moduleImportPath.get(0);
	}

	@Override
	public String boundObjectName() {
		return moduleImportPath.get(0);
	}

	@Override
	public ImportPath loadedPath() {
		return moduleImportPath;
	}

	@Override
	public ImportPath boundObjectParentPath() {
		return ImportPath.EMPTY_PATH;
	}

	@Override
	public boolean importsAreLimitedToModules() {
		return true;
	}

	/**
	 * Creates new standard (non-from) import.
	 * 
	 * @param moduleImportPath
	 *            the relative path of the module being imported
	 */
	private StandardImportSpecification(ImportPath moduleImportPath) {
		if (moduleImportPath == null)
			throw new NullPointerException("Module path is not optional");
		if (moduleImportPath.isEmpty())
			throw new IllegalArgumentException("Module path cannot be empty");

		this.moduleImportPath = moduleImportPath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((moduleImportPath == null) ? 0 : moduleImportPath.hashCode());
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
		StandardImportSpecification other = (StandardImportSpecification) obj;
		if (moduleImportPath == null) {
			if (other.moduleImportPath != null)
				return false;
		} else if (!moduleImportPath.equals(other.moduleImportPath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "import " + moduleImportPath;
	}

}
