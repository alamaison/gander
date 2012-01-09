package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;

final class FromImportInfo implements ImportInfo {

	static FromImportInfo newInstance(String moduleImportName, String itemName) {
		return new FromImportInfo(moduleImportName, itemName);
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

	public <O, C, M> ModuleBindingScheme<M> newBindingScheme(M relativeTo,
			C outerImportReceiver, Binder<O, C, M> bindingHandler,
			Loader<O, C, M> loader) {
		return new FromImportAsScheme<O, C, M>(relativeTo, outerImportReceiver, itemName,
				bindingHandler, loader, this);
	}

	private FromImportInfo(String moduleImportName, String itemName) {
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
		FromImportInfo other = (FromImportInfo) obj;
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
