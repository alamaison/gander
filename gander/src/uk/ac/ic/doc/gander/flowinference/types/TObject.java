package uk.ac.ic.doc.gander.flowinference.types;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.typegoals.NamespaceNameTypeGoal;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.OldNamespace;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.ObjectInstanceNamespace;
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
	 */
	public Result<Type> memberType(String memberName, SubgoalManager goalManager) {

		RedundancyEliminator<Type> result = new RedundancyEliminator<Type>();

		result.add(memberTypeFromObject(memberName, goalManager));

		if (!result.isFinished()) {
			result.add(memberTypeFromClass(memberName, goalManager));
		}

		return result.result();
	}

	private Result<Type> memberTypeFromObject(String memberName,
			SubgoalManager goalManager) {
		return memberTypeFromNamespace(memberName, new ObjectInstanceNamespace(
				classObject), goalManager);
	}

	private Result<Type> memberTypeFromClass(String memberName,
			SubgoalManager goalManager) {
		Result<Type> unboundTypes = memberTypeFromNamespace(memberName,
				classObject.fullyQualifiedNamespace(), goalManager);

		return unboundTypes.transformResult(new TypeBinder());
	}

	private Result<Type> memberTypeFromNamespace(String memberName,
			OldNamespace namespace, SubgoalManager goalManager) {
		NamespaceName member = new NamespaceName(memberName, namespace);
		return goalManager.registerSubgoal(new NamespaceNameTypeGoal(member));
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
	}

	private Type bindType(Type unboundType) {
		if (unboundType instanceof TFunction) {
			return new TBoundMethod(((TFunction) unboundType).codeObject(),
					this);
		} else {
			return unboundType;
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Object instances are summarised using their class's namespace so a member
	 * in once instance will affect all instances.
	 */
	public Set<OldNamespace> memberReadableNamespaces() {
		return Collections.<OldNamespace> singleton(classObject
				.fullyQualifiedNamespace());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Object instances are summarised using their class's namespace so a member
	 * in once instance will affect all instances.
	 */
	public OldNamespace memberWriteableNamespace() {
		return classObject.fullyQualifiedNamespace();
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
