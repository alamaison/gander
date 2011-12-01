package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Processor;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TNamespace;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.NamespaceName;

final class NamespaceNameTypeGoal implements TypeGoal {

	private NamespaceName name;

	NamespaceNameTypeGoal(NamespaceName name) {
		if (name == null)
			throw new NullPointerException(
					"Can't find an name's type if we don't have a name");

		this.name = name;
	}

	public Result<Type> initialSolution() {
		return FiniteResult.bottom();
	}

	public Result<Type> recalculateSolution(SubgoalManager goalManager) {

		return new NamespaceNameTypeGoalSolver(name, goalManager).solution();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		NamespaceNameTypeGoal other = (NamespaceNameTypeGoal) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NamespaceNameTypeGoal [name=" + name + "]";
	}

}

final class NamespaceNameTypeGoalSolver {

	private final NamespaceName name;
	private final SubgoalManager goalManager;

	private RedundancyEliminator<Type> completeType = new RedundancyEliminator<Type>();

	NamespaceNameTypeGoalSolver(NamespaceName name, SubgoalManager goalManager) {
		if (name == null)
			throw new NullPointerException(
					"Can't find an name's type if we don't have a name");
		if (goalManager == null)
			throw new NullPointerException(
					"We need to be able to issue subqueries");

		this.name = name;
		this.goalManager = goalManager;

		addTypesFromNamespace();
		if (name.namespace() instanceof Class) {
			addTypesFromInheritanceChain((Class) name.namespace(), name.name());
		}
	}

	Result<Type> solution() {
		return completeType.result();
	}

	private void addTypesFromNamespace() {
		completeType.add(new UnqualifiedNameDefinitionsPartialSolution(
				goalManager, name).partialSolution());
		completeType.add(new QualifiedNameDefinitionsPartialSolution(
				goalManager, name).partialSolution());
	}

	private void addTypesFromInheritanceChain(Class klass, String name) {

		for (exprType supertype : klass.inheritsFrom()) {

			/*
			 * XXX: HACK: We don't to search the whole inheritance tree when
			 * inferring the type of a member so we stop if they are methods or
			 * nested classes which override anything in the superclass. There
			 * must be a better way to do this that also takes account of data
			 * members being specific to a sub class etc.
			 */
			if (foundDeclaredMember()) {
				break;
			}

			Result<Type> supertypeTypes = goalManager
					.registerSubgoal(new ExpressionTypeGoal(
							new ModelSite<exprType>(supertype, klass
									.getParentScope().codeObject())));

			MemberTyper memberTyper = new MemberTyper();
			supertypeTypes.actOnResult(memberTyper);
		}
	}

	private boolean foundDeclaredMember() {

		final boolean found[] = { false };

		completeType.result().actOnResult(new Processor<Type>() {

			public void processInfiniteResult() {
				found[0] = true;
			}

			public void processFiniteResult(Set<Type> result) {
				for (Type type : result) {
					if (isDeclaredType(type)) {
						found[0] = true;
						break;
					}
				}
			}
		});

		return found[0];
	}

	protected boolean isDeclaredType(Type type) {
		return type instanceof TNamespace;
	}

	private final class MemberTyper implements Processor<Type> {

		public void processInfiniteResult() {
			// do nothing, no member found
		}

		public void processFiniteResult(Set<Type> result) {
			for (Type supertypeType : result) {
				if (completeType.isFinished())
					break;

				if (supertypeType instanceof TClass) {
					Class superclass = ((TClass) supertypeType)
							.getClassInstance();

					Result<Type> inheritedType = goalManager
							.registerSubgoal(new NamespaceNameTypeGoal(
									new NamespaceName(name.name(), superclass)));
					completeType.add(inheritedType);
				}
			}
		}
	}
}
