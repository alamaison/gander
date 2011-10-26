package uk.ac.ic.doc.gander.flowinference.modelgoals;

import java.util.Collections;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.model.Model;

/**
 * Collect all call sites in the loaded model.
 * 
 * Takes no account of call target type, number of parameters, namespace or
 * anything else. If it has parameter brackets after it, it gets included.
 */
public class AllCallSitesGoal implements ModelGoal<Call> {

	private final Model model;

	public AllCallSitesGoal(Model model) {
		this.model = model;
	}

	public Set<ModelSite<Call>> initialSolution() {
		return Collections.emptySet();
	}

	public Set<ModelSite<Call>> recalculateSolution(
			final SubgoalManager goalManager) {

		return new AllCallSitesFinder(model).getSites();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((model == null) ? 0 : model.hashCode());
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
		AllCallSitesGoal other = (AllCallSitesGoal) obj;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AllCallSitesGoal";
	}

}
