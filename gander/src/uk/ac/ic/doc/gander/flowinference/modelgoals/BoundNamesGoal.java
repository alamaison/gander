package uk.ac.ic.doc.gander.flowinference.modelgoals;

import java.util.Collections;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.name_binding.InScopeNameFinder;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

/**
 * Goal for {@link Name}s that bind to the given token in the given namespace.
 */
public final class BoundNamesGoal implements ModelGoal<Name> {

	private final Variable namespaceKey;

	public BoundNamesGoal(Variable nameBinding) {
		this.namespaceKey = nameBinding;
	}

	public Set<ModelSite<Name>> initialSolution() {
		return Collections.emptySet();
	}

	public Set<ModelSite<Name>> recalculateSolution(SubgoalManager goalManager) {
		return new InScopeNameFinder(namespaceKey).getNameBindings();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((namespaceKey == null) ? 0 : namespaceKey.hashCode());
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
		BoundNamesGoal other = (BoundNamesGoal) obj;
		if (namespaceKey == null) {
			if (other.namespaceKey != null)
				return false;
		} else if (!namespaceKey.equals(other.namespaceKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BoundNamesGoal [namespaceKey=" + namespaceKey + "]";
	}

}
