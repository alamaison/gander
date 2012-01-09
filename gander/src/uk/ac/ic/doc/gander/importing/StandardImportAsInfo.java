package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;

final class StandardImportAsInfo implements ImportInfo {

	private final String moduleImportName;
	private final String alias;

	static StandardImportAsInfo newInstance(String moduleImportName,
			String alias) {
		return new StandardImportAsInfo(moduleImportName, alias);
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

	public <O, C, M> ModuleBindingScheme<M> newBindingScheme(Import<C, M> importInstance,
			Binder<O, C, M> bindingHandler, Loader<O, C, M> loader) {
		return new ImportAsScheme<O, C, M>(importInstance, alias,
				bindingHandler);
	}

	private StandardImportAsInfo(String moduleImportName, String alias) {
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
		StandardImportAsInfo other = (StandardImportAsInfo) obj;
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
