package uk.ac.ic.doc.gander.flowinference.types;

import java.util.Collections;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.NamespaceNameTypeGoal;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;

public class TClass implements TCodeObject, TCallable {

	private final ClassCO classObject;

	public TClass(ClassCO classInstance) {
		if (classInstance == null) {
			throw new NullPointerException("Code object required");
		}

		this.classObject = classInstance;
	}

	public ClassCO codeObject() {
		return classObject;
	}

	@Deprecated
	public TClass(Class classInstance) {
		this(classInstance.codeObject());
	}

	@Deprecated
	public Class getClassInstance() {
		return classObject.oldStyleConflatedNamespace();
	}

	public String getName() {
		return getClassInstance().getFullName();
	}

	public Result<Type> returnType(SubgoalManager goalManager) {
		/*
		 * Calling a class is a constructor call. Constructors are special
		 * functions so we can infer the return type immediately. It is an
		 * instance of the class being called.
		 */
		return new FiniteResult<Type>(Collections.singleton(new TObject(
				classObject)));
	}

	public int passedArgumentOffset() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Members on a class are returned directly from the class namespace.
	 */
	public Result<Type> memberType(String memberName, SubgoalManager goalManager) {

		NamespaceName member = new NamespaceName(memberName, classObject
				.fullyQualifiedNamespace());
		return goalManager.registerSubgoal(new NamespaceNameTypeGoal(member));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((classObject == null) ? 0 : classObject.hashCode());
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
		TClass other = (TClass) obj;
		if (classObject == null) {
			if (other.classObject != null)
				return false;
		} else if (!classObject.equals(other.classObject))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TClass [" + getName() + "]";
	}

}
