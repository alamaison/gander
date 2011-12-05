package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;

public class TModule implements TImportable, TNamespace, TCodeObject {

	private final ModuleCO moduleInstance;

	public TModule(ModuleCO moduleInstance) {
		if (moduleInstance == null) {
			throw new NullPointerException("Code object required");
		}

		this.moduleInstance = moduleInstance;
	}

	public ModuleCO codeObject() {
		return moduleInstance;
	}

	@Deprecated
	public TModule(Module loaded) {
		this(loaded.codeObject());
	}

	@Deprecated
	public Module getModuleInstance() {
		return moduleInstance.oldStyleConflatedNamespace();
	}

	@Deprecated
	public Namespace getNamespaceInstance() {
		return getModuleInstance();
	}

	public String getName() {
		return getNamespaceInstance().getFullName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((moduleInstance == null) ? 0 : moduleInstance.hashCode());
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
		TModule other = (TModule) obj;
		if (moduleInstance == null) {
			if (other.moduleInstance != null)
				return false;
		} else if (!moduleInstance.equals(other.moduleInstance))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TModule [" + getName() + "]";
	}
}
