package uk.ac.ic.doc.gander.importing;

/**
 * Model of an import statement of the form {@code import x.y.z as bar}.
 */
final class StandardImportAsStatement implements StaticImportStatement {

	private final ImportPath moduleImportPath;
	private final String alias;

	/**
	 * Creates representation of a standard (non-from) import statement with an
	 * alias.
	 * 
	 * @param moduleImportPath
	 *            the path of the module being imported relative to the code
	 *            block in which it appeared (really relative to that code
	 *            block's containing module)
	 * @param alias
	 *            the name that the first segment of the path is bound to with
	 *            respect to the container
	 */
	static StandardImportAsStatement newInstance(ImportPath moduleImportPath,
			String alias) {
		return new StandardImportAsStatement(moduleImportPath, alias);
	}

	@Override
	public ImportPath boundObjectParentPath() {
		return moduleImportPath.subPath(0, moduleImportPath.size() - 1);
	}

	@Override
	public String boundObjectName() {
		return moduleImportPath.get(moduleImportPath.size() - 1);
	}

	@Override
	public String bindingName() {
		return alias;
	}

	@Override
	public ImportPath modulePath() {
		return moduleImportPath;
	}

	@Override
	public boolean importsAreLimitedToModules() {
		return true;
	}

	@Override
	public BindingScheme bindingScheme() {

		return StandardImportAsBindingScheme.INSTANCE;
	}

	private StandardImportAsStatement(ImportPath moduleImportPath, String alias) {
		if (moduleImportPath == null)
			throw new NullPointerException("Module path is not optional");
		if (moduleImportPath.isEmpty())
			throw new IllegalArgumentException("Module path cannot be empty");
		if (alias == null)
			throw new NullPointerException("Alias is not optional");
		if (alias.isEmpty())
			throw new IllegalArgumentException("Alias name cannot be empty");

		this.moduleImportPath = moduleImportPath;
		this.alias = alias;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
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
		StandardImportAsStatement other = (StandardImportAsStatement) obj;
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
			return false;
		if (moduleImportPath == null) {
			if (other.moduleImportPath != null)
				return false;
		} else if (!moduleImportPath.equals(other.moduleImportPath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "import " + moduleImportPath + " as " + alias;
	}

}
