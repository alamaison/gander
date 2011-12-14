package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.NamespaceNameTypeGoal;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;
import uk.ac.ic.doc.gander.model.codeobject.FunctionCO;

public final class TBoundMethod implements TCallable {

	private final FunctionCO unboundMethod;
	private final ClassCO instanceType;

	public TBoundMethod(FunctionCO unboundMethod, ClassCO instanceType) {
		if (unboundMethod == null)
			throw new NullPointerException(
					"Bound method must have a corresponding unbound method");
		if (instanceType == null)
			throw new NullPointerException(
					"Bound methods are bound to an instance of a class");

		assert unboundMethod.parent() instanceof ClassCO;

		this.unboundMethod = unboundMethod;
		this.instanceType = instanceType;
	}

	/**
	 * Returns the function that has been bound.
	 */
	FunctionCO unboundMethod() {
		return unboundMethod;
	}

	/**
	 * Returns the type of the object that is bound to this method.
	 */
	ClassCO instanceType() {
		return instanceType;
	}

	public String getName() {
		return "<bound method " + instanceType.declaredName() + "."
				+ unboundMethod.declaredName() + ">";
	}

	public Result<Type> returnType(SubgoalManager goalManager) {
		return new FunctionReturnTypeSolver(goalManager, unboundMethod)
				.solution();
	}

	public int passedArgumentOffset() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Members on a method are returned directly from the method's function
	 * object's namespace.
	 */
	public Result<Type> memberType(String memberName, SubgoalManager goalManager) {

		NamespaceName member = new NamespaceName(memberName, unboundMethod
				.fullyQualifiedNamespace());
		return goalManager.registerSubgoal(new NamespaceNameTypeGoal(member));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((instanceType == null) ? 0 : instanceType.hashCode());
		result = prime * result
				+ ((unboundMethod == null) ? 0 : unboundMethod.hashCode());
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
		TBoundMethod other = (TBoundMethod) obj;
		if (instanceType == null) {
			if (other.instanceType != null)
				return false;
		} else if (!instanceType.equals(other.instanceType))
			return false;
		if (unboundMethod == null) {
			if (other.unboundMethod != null)
				return false;
		} else if (!unboundMethod.equals(other.unboundMethod))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TBoundMethod [" + getName() + "]";
	}

}
