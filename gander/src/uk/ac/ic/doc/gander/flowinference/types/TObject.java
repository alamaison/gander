package uk.ac.ic.doc.gander.flowinference.types;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.typegoals.NamespaceNameTypeGoal;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;

public class TObject implements Type {

	private final ClassCO classObject;

	@Deprecated
	public TObject(Class classInstance) {
		if (classInstance == null)
			throw new NullPointerException("Class object required");
		this.classObject = classInstance.codeObject();
	}

	public TObject(ClassCO classObject) {
		if (classObject == null)
			throw new NullPointerException("Class object required");
		this.classObject = classObject;
	}

	@Deprecated
	public Class getClassInstance() {
		return classObject.oldStyleConflatedNamespace();
	}

	public ClassCO classObject() {
		return classObject;
	}

	public String getName() {
		return "Instance<" + getClassInstance().getFullName() + ">";
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Members on an object are converted to bound method objects before they
	 * are returned from the namespace.
	 * 
	 * FIXME: This isn't completely true. It only happens if the member came
	 * from the class object's namespace, rather than the object itself.
	 */
	public Result<Type> memberType(String memberName, SubgoalManager goalManager) {

		NamespaceName member = new NamespaceName(memberName, classObject
				.fullyQualifiedNamespace());
		Result<Type> unboundTypes = goalManager
				.registerSubgoal(new NamespaceNameTypeGoal(member));

		return unboundTypes.transformResult(new TypeBinder());
	}

	private final class TypeBinder implements Transformer<Type, Result<Type>> {

		public Result<Type> transformFiniteResult(Set<Type> result) {
			Set<Type> boundTypes = new HashSet<Type>();

			for (Type unboundType : result) {
				boundTypes.add(bindType(unboundType));
			}

			return new FiniteResult<Type>(boundTypes);
		}

		public Result<Type> transformInfiniteResult() {
			return TopT.INSTANCE;
		}

		private Type bindType(Type unboundType) {
			if (unboundType instanceof TFunction) {
				return new TBoundMethod(((TFunction) unboundType).codeObject(),
						classObject);
			} else {
				return unboundType;
			}
		}
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
		TObject other = (TObject) obj;
		if (classObject == null) {
			if (other.classObject != null)
				return false;
		} else if (!classObject.equals(other.classObject))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getName();
	}
}
