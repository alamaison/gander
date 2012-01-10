package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;

final class FromImportSpecification implements ImportSpecification {

	/**
	 * Creates new from-style import.
	 * 
	 * @param moduleImportName
	 *            the relative path of the module whose namespace item is being
	 *            imported
	 * @param itemName
	 *            the name of the item being imported
	 */
	static FromImportSpecification newInstance(String moduleImportName,
			String itemName) {
		return new FromImportSpecification(moduleImportName, itemName);
	}

	private final String moduleImportName;
	private final String itemName;

	public String bindingName() {
		return LocallyBoundImportNameResolver.resolveFromImport(
				moduleImportName, itemName);
	}

	public String bindingObject() {
		return LocallyBoundImportObjectResolver.resolveFromImport(
				moduleImportName, itemName);
	}

	public ImportPath objectPath() {
		return ImportPath.fromDottedName(moduleImportName + "." + itemName);
	}

	public <O, C, M> ModuleBindingScheme<M> newBindingScheme(
			Import<C, M> importInstance, Binder<O, C, M> bindingHandler,
			Loader<O, C, M> loader) {
		return new FromImportAsScheme<O, C, M>(importInstance, itemName,
				bindingHandler, loader);
	}

	/**
	 * Creates new from-style import.
	 * 
	 * @param moduleImportName
	 *            the relative path of the module whose namespace item is being
	 *            imported
	 * @param itemName
	 *            the name of the item being imported
	 */
	private FromImportSpecification(String moduleImportName, String itemName) {
		if (moduleImportName == null)
			throw new NullPointerException("Module path is not optional");
		if (moduleImportName.isEmpty())
			throw new IllegalArgumentException("Module path cannot be empty");
		if (itemName == null)
			throw new NullPointerException("Item name is not optional");
		if (itemName.isEmpty())
			throw new IllegalArgumentException("Item name cannot be empty");

		this.moduleImportName = moduleImportName;
		this.itemName = itemName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		FromImportSpecification other = (FromImportSpecification) obj;
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
		return "from " + moduleImportName + " import " + itemName;
	}

}
