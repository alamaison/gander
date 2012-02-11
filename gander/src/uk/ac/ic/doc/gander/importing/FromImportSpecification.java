package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;

/**
 * Model of an import statement of the form {@code from x.y import i}.
 */
final class FromImportSpecification implements StaticImportSpecification {

	/**
	 * Creates representation of a from-style import statement.
	 * 
	 * @param moduleImportPath
	 *            the path of the module with respect to which an item is being
	 *            imported; relative to code block in which the import statement
	 *            appeared (really relative to that code block's containing
	 *            module)
	 * @param itemName
	 *            the name of the item being imported
	 */
	static FromImportSpecification newInstance(ImportPath moduleImportName,
			String itemName) {
		return new FromImportSpecification(moduleImportName, itemName);
	}

	private final ImportPath moduleImportPath;
	private final String itemName;

	@Override
	public String bindingName() {
		return itemName;
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

	@Override
	public <O, C, M> BindingScheme<M> newBindingScheme(
			Import<O, C, M> importInstance, Binder<O, C, M> bindingHandler,
			Loader<O, M> loader) {

		// The non-aliased from-import shares the from-import-as binding
		// scheme
		return new FromImportAsBindingScheme<O, C, M>(importInstance,
				bindingHandler, loader);
	}

	private FromImportSpecification(ImportPath moduleImportPath, String itemName) {
		if (moduleImportPath == null)
			throw new NullPointerException("Module path is not optional");
		if (moduleImportPath.isEmpty())
			throw new IllegalArgumentException("Module path cannot be empty");
		if (itemName == null)
			throw new NullPointerException("Item name is not optional");
		if (itemName.isEmpty())
			throw new IllegalArgumentException("Item name cannot be empty");

		this.moduleImportPath = moduleImportPath;
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
		FromImportSpecification other = (FromImportSpecification) obj;
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
		return "from " + moduleImportPath + " import " + itemName;
	}

}
