package uk.ac.ic.doc.gander.flowinference.modelgoals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.model.Model;

public class CardinalCallSitesGoal implements ModelGoal<Call> {

	private final Model model;
	private final int cardinality;

	// not significant for identity - derived from model
	private Set<ModelSite<Call>> solution = null;

	public CardinalCallSitesGoal(Model model, int cardinality) {
		this.model = model;
		this.cardinality = cardinality;
		this.solution = new CardinalCallSitesFinder(model, cardinality)
				.getSites();
	}

	public Set<ModelSite<Call>> initialSolution() {
		return Collections.emptySet();
	}

	public Set<ModelSite<Call>> recalculateSolution(
			final SubgoalManager goalManager) {
		if (solution == null) {
			System.out.println("Finding all call sites with cardinality "
					+ cardinality);

			Set<ModelSite<Call>> sites = new HashSet<ModelSite<Call>>();
			for (ModelSite<Call> callSite : goalManager
					.registerSubgoal(new AllCallSitesGoal(model))) {
				if (callSite.getNode().args.length == cardinality) {
					sites.add(callSite);
				}
			}
			System.out.println("# sites" + sites.size());
			solution = sites;
		}

		return solution;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cardinality;
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
		CardinalCallSitesGoal other = (CardinalCallSitesGoal) obj;
		if (cardinality != other.cardinality)
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CardinalCallSitesGoal [cardinality=" + cardinality + "]";
	}

}
