package uk.ac.ic.doc.gander.flowinference.modelgoals;

import java.util.Collections;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.sitefinders.CallSitesFinder;

/**
 * Find call sites matching a predicate.
 */
public class CallSitesGoal implements ModelGoal<Call> {

	private final Model model;
	private final Predicate predicate;

	public interface Predicate {
		boolean isMatch(ModelSite<Call> callSite);
	}

	public CallSitesGoal(Model model, Predicate predicate) {
		this.model = model;
		this.predicate = predicate;
	}

	public Set<ModelSite<Call>> initialSolution() {
		return Collections.emptySet();
	}

	public Set<ModelSite<Call>> recalculateSolution(
			final SubgoalManager goalManager) {
		return new CallSitesFinder(model, new CallSitesFinder.Predicate(){

			public boolean isMatch(ModelSite<Call> callSite) {
				return predicate.isMatch(callSite);
			}

		}).getSites();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result
				+ ((predicate == null) ? 0 : predicate.hashCode());
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
		CallSitesGoal other = (CallSitesGoal) obj;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (predicate == null) {
			if (other.predicate != null)
				return false;
		} else if (!predicate.equals(other.predicate))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CallSitesGoal [predicate=" + predicate + "]";
	}

}
