package uk.ac.ic.doc.gander.flowinference.typegoals.namespacename;



import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.TypeGoal;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.NamespaceName;

public final class NamespaceNameTypeGoal implements TypeGoal {

	private NamespaceName name;

	public NamespaceNameTypeGoal(NamespaceName name) {
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
