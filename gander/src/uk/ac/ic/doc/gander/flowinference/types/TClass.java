package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Namespace;

public class TClass implements TNamespace {
	
	private Class classInstance;

	public TClass(Class classInstance) {
		assert classInstance != null;
		this.classInstance = classInstance;
	}

	public Class getClassInstance() {
		return classInstance;
	}

	public Namespace getNamespaceInstance() {
		return getClassInstance();
	}

	public String getName() {
		return getNamespaceInstance().getFullName();
	}

}
