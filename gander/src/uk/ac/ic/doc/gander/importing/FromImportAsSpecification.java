package uk.ac.ic.doc.gander.importing;

final class FromImportAsSpecification implements StaticImportSpecification {

	/**
	 * Creates new from-style import with alias.
	 * 
	 * @param moduleImportPath
	 *            the relative path of the module whose namespace item is being
	 *            imported
	 * @param itemName
	 *            the name of the item being imported
	 * @param alias
	 *            the name that imported item is bound to with respect to the
	 *            container
	 */
	static FromImportAsSpecification newInstance(String moduleImportPath,
			String itemName, String alias) {
		return new FromImportAsSpecification(
				ImportPath.fromDottedName(moduleImportPath), itemName, alias);
	}

	private final ImportPath moduleImportPath;
	private final String itemName;
	private final String alias;

	@Override
	public String bindingName() {
		return alias;
	}

	@Override
	public ImportPath loadedPath() {
		return moduleImportPath.append(itemName);
	}

	@Override
	public ImportPath boundObjectParentPath() {
		return moduleImportPath;
	}

	@Override
	public String boundObjectName() {
		return itemName;
	}

	@Override
	public boolean importsAreLimitedToModules() {
		return false;
	}

	/**
	 * Creates new from-style import with alias.
	 * 
	 * @param moduleImportPath
	 *            the relative path of the module whose namespace item is being
	 *            imported
	 * @param itemName
	 *            the name of the item being imported
	 * @param alias
	 *            the name that imported item is bound to with respect to the
	 *            container
	 */
	private FromImportAsSpecification(ImportPath moduleImportPath,
			String itemName, String alias) {
		if (moduleImportPath == null)
			throw new NullPointerException("Module path is not optional");
		if (moduleImportPath.isEmpty())
			throw new IllegalArgumentException("Module path cannot be empty");
		if (itemName == null)
			throw new NullPointerException("Item name is not optional");
		if (itemName.isEmpty())
			throw new IllegalArgumentException("Item name cannot be empty");
		if (alias == null)
			throw new NullPointerException("Alias is not optional");
		if (alias.isEmpty())
			throw new IllegalArgumentException("Alias name cannot be empty");

		this.moduleImportPath = moduleImportPath;
		this.itemName = itemName;
		this.alias = alias;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		result = prime * result
				+ ((itemName == null) ? 0 : itemName.hashCode());
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
		FromImportAsSpecification other = (FromImportAsSpecification) obj;
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
			return false;
		if (itemName == null) {
			if (other.itemName != null)
				return false;
		} else if (!itemName.equals(other.itemName))
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
		return "from " + moduleImportPath + " import " + itemName + " as "
				+ alias;
	}

}
