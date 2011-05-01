package uk.ac.ic.doc.gander;

import uk.ac.ic.doc.gander.analysers.DuckHunt;
import uk.ac.ic.doc.gander.analysers.Tallies;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;

public class RunDuckHunt extends HierarchyTallyRunner {

	public static void main(String[] args) throws Exception {
		new RunDuckHunt().run(args);
	}

	@Override
	protected String getCategoryTitle() {
		return "Number of types inferred for a single call target";
	}

	@Override
	protected String getTitle() {
		return "Uniqueness of duck-inferred types";
	}

	@Override
	protected Tallies analyse(Hierarchy hierarchy) throws Exception {
		DuckHunt analysis = new DuckHunt(hierarchy);
		return analysis.getResult();
	}
}
