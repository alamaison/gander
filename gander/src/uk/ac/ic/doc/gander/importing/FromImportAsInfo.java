package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;

final class FromImportAsInfo implements ImportInfo {

	static FromImportAsInfo newInstance(String moduleImportName,
			String itemName, String alias) {
		return new FromImportAsInfo(moduleImportName, itemName, alias);
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

	public <O, C, M> ModuleBindingScheme<M> newBindingScheme(C outerImportReceiver,
			Binder<O, C, M> bindingHandler, Loader<O, C, M> loader) {
		return new FromImportAsScheme<O, C, M>(outerImportReceiver, alias,
				bindingHandler, loader);
	}

	private FromImportAsInfo(String moduleImportName, String itemName,
			String alias) {
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
		FromImportAsInfo other = (FromImportAsInfo) obj;
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
