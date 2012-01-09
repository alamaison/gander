package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;

final class StandardImportInfo implements ImportInfo {

	private final String moduleImportName;

	static StandardImportInfo newInstance(String moduleImportName) {
		return new StandardImportInfo(moduleImportName);
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

	public <O, C, M> ModuleBindingScheme<M> newBindingScheme(C outerImportReceiver,
			Binder<O, C, M> bindingHandler, Loader<O, C, M> loader) {
		return new ImportScheme<O, C, M>(outerImportReceiver, bindingHandler);
	}

	private StandardImportInfo(String moduleImportName) {
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
		StandardImportInfo other = (StandardImportInfo) obj;
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
