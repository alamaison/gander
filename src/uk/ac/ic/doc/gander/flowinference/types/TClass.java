package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Scope;

public class TClass implements ScopeType {
	
	private Class classInstance;

	public TClass(Class classInstance) {
		this.classInstance = classInstance;
	}

	public Class getClassInstance() {
		return classInstance;
	}

	public Scope getScopeInstance() {
		return getClassInstance();
	}

}
