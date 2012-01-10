package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;

final class StandardImportSpecification implements ImportSpecification {

	private final String moduleImportName;

	/**
	 * Creates new standard (non-from) import.
	 * 
	 * @param moduleImportName
	 *            the relative path of the module being imported
	 */
	static StandardImportSpecification newInstance(String moduleImportName) {
		return new StandardImportSpecification(moduleImportName);
	}

	public String bindingName() {
		return LocallyBoundImportNameResolver.resolveImport(moduleImportName);
	}

	public String bindingObject() {
		return LocallyBoundImportObjectResolver.resolveImport(moduleImportName);
	}

	public ImportPath objectPath() {
		return ImportPath.fromDottedName(moduleImportName);
	}

	public <O, C, M> ModuleBindingScheme<M> newBindingScheme(
			Import<C, M> importInstance, Binder<O, C, M> bindingHandler,
			Loader<O, C, M> loader) {
		return new StandardImportScheme<O, C, M>(importInstance, bindingHandler);
	}

	/**
	 * Creates new standard (non-from) import.
	 * 
	 * @param moduleImportName
	 *            the relative path of the module being imported
	 */
	private StandardImportSpecification(String moduleImportName) {
		if (moduleImportName == null)
			throw new NullPointerException("Module path is not optional");
		if (moduleImportName.isEmpty())
			throw new IllegalArgumentException("Module path cannot be empty");

		this.moduleImportName = moduleImportName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		StandardImportSpecification other = (StandardImportSpecification) obj;
		if (moduleImportName == null) {
			if (other.moduleImportName != null)
				return false;
		} else if (!moduleImportName.equals(other.moduleImportName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "import " + moduleImportName;
	}

}
