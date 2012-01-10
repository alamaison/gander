package uk.ac.ic.doc.gander.importing;

final class FromImportAsSpecification implements ImportSpecification {

	/**
	 * Creates new from-style import with alias.
	 * 
	 * @param moduleImportName
	 *            the relative path of the module whose namespace item is being
	 *            imported
	 * @param itemName
	 *            the name of the item being imported
	 * @param alias
	 *            the name that imported item is bound to with respect to the
	 *            container
	 */
	static FromImportAsSpecification newInstance(String moduleImportName,
			String itemName, String alias) {
		return new FromImportAsSpecification(moduleImportName, itemName, alias);
	}

	private final String moduleImportName;
	private final String itemName;
	private final String alias;

	public String bindingName() {
		return LocallyBoundImportNameResolver.resolveFromImportAs(
				moduleImportName, itemName, alias);
	}

	public String bindingObject() {
		return LocallyBoundImportObjectResolver.resolveFromImportAs(
				moduleImportName, itemName, alias);
	}

	public ImportPath objectPath() {
		return ImportPath.fromDottedName(moduleImportName + "." + itemName);
	}

	/**
	 * Creates new from-style import with alias.
	 * 
	 * @param moduleImportName
	 *            the relative path of the module whose namespace item is being
	 *            imported
	 * @param itemName
	 *            the name of the item being imported
	 * @param alias
	 *            the name that imported item is bound to with respect to the
	 *            container
	 */
	private FromImportAsSpecification(String moduleImportName, String itemName,
			String alias) {
		if (moduleImportName == null)
			throw new NullPointerException("Module path is not optional");
		if (moduleImportName.isEmpty())
			throw new IllegalArgumentException("Module path cannot be empty");
		if (itemName == null)
			throw new NullPointerException("Item name is not optional");
		if (itemName.isEmpty())
			throw new IllegalArgumentException("Item name cannot be empty");
		if (alias == null)
			throw new NullPointerException("Alias is not optional");
		if (alias.isEmpty())
			throw new IllegalArgumentException("Alias name cannot be empty");
			
		this.moduleImportName = moduleImportName;
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
				+ ((moduleImportName == null) ? 0 : moduleImportName.hashCode());
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
		if (moduleImportName == null) {
			if (other.moduleImportName != null)
				return false;
		} else if (!moduleImportName.equals(other.moduleImportName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "from " + moduleImportName + " import " + itemName + " as "
				+ alias;
	}

}
