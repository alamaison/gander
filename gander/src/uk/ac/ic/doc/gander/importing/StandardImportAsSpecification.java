package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;

final class StandardImportAsSpecification implements ImportSpecification {

	private final String moduleImportName;
	private final String alias;

	/**
	 * Creates new standard (non-from) import with alias.
	 * 
	 * @param moduleImportName
	 *            the relative path of the module being imported
	 * @param alias
	 *            the name that the first segment of the path is bound to with
	 *            respect to the container
	 */
	static StandardImportAsSpecification newInstance(String moduleImportName,
			String alias) {
		return new StandardImportAsSpecification(moduleImportName, alias);
	}

	public String bindingName() {
		return LocallyBoundImportNameResolver.resolveImportAs(moduleImportName,
				alias);
	}

	public String bindingObject() {
		return LocallyBoundImportObjectResolver.resolveImportAs(
				moduleImportName, alias);
	}

	public ImportPath objectPath() {
		return ImportPath.fromDottedName(moduleImportName);
	}

	/**
	 * Creates new standard (non-from) import with alias.
	 * 
	 * @param moduleImportName
	 *            the relative path of the module being imported
	 * @param alias
	 *            the name that the first segment of the path is bound to with
	 *            respect to the container
	 */
	private StandardImportAsSpecification(String moduleImportName, String alias) {
		if (moduleImportName == null)
			throw new NullPointerException("Module path is not optional");
		if (moduleImportName.isEmpty())
			throw new IllegalArgumentException("Module path cannot be empty");
		if (alias == null)
			throw new NullPointerException("Alias is not optional");
		if (alias.isEmpty())
			throw new IllegalArgumentException("Alias name cannot be empty");
			
		this.moduleImportName = moduleImportName;
		this.alias = alias;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
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
		StandardImportAsSpecification other = (StandardImportAsSpecification) obj;
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
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
		return "import " + moduleImportName + " as " + alias;
	}

}
