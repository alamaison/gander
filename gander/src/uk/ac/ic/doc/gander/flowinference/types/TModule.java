package uk.ac.ic.doc.gander.flowinference.types;

import java.util.Collections;
import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.NamespaceNameTypeGoal;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.OldNamespace;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;

public class TModule implements TCodeObject {

	private final ModuleCO moduleObject;

	public TModule(ModuleCO moduleInstance) {
		if (moduleInstance == null) {
			throw new NullPointerException("Code object required");
		}

		this.moduleObject = moduleInstance;
	}

	public ModuleCO codeObject() {
		return moduleObject;
	}

	@Deprecated
	public TModule(Module loaded) {
		this(loaded.codeObject());
	}

	@Deprecated
	public Module getModuleInstance() {
		return moduleObject.oldStyleConflatedNamespace();
	}

	public String getName() {
		return getModuleInstance().getFullName();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Members on a module are returned directly from the module object's
	 * namespace.
	 */
	public Result<Type> memberType(String memberName, SubgoalManager goalManager) {

		NamespaceName member = new NamespaceName(memberName, moduleObject
				.fullyQualifiedNamespace());
		return goalManager.registerSubgoal(new NamespaceNameTypeGoal(member));
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<OldNamespace> memberReadableNamespaces() {
		return Collections.<OldNamespace> singleton(moduleObject
				.fullyQualifiedNamespace());
	}

	/**
	 * {@inheritDoc}
	 */
	public OldNamespace memberWriteableNamespace() {
		return moduleObject.fullyQualifiedNamespace();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((moduleObject == null) ? 0 : moduleObject.hashCode());
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
		if (moduleObject == null) {
			if (other.moduleObject != null)
				return false;
		} else if (!moduleObject.equals(other.moduleObject))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TModule [" + getName() + "]";
	}
}
