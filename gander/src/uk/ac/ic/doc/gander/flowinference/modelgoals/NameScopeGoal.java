package uk.ac.ic.doc.gander.flowinference.modelgoals;

import java.util.Collections;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.name_binding.NameScopeFinder;

/**
 * Goal finding which variables (AST {@link Name}s) bind in the given namespace.
 * 
 * In other words, which are in this namespace's scope for the given name.
 */
public final class NameScopeGoal implements ModelGoal<Name> {

	private final NamespaceName name;

	public NameScopeGoal(NamespaceName namespaceName) {
		this.name = namespaceName;
	}

	public Set<ModelSite<Name>> initialSolution() {
		return Collections.emptySet();
	}

	public Set<ModelSite<Name>> recalculateSolution(SubgoalManager goalManager) {
		return new NameScopeFinder(name).getNameBindings();
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
		NameScopeGoal other = (NameScopeGoal) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NameScopeGoal [name=" + name + "]";
	}

}
