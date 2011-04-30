package uk.ac.ic.doc.gander;

import java.io.File;

import uk.ac.ic.doc.gander.analysers.DuckHunt;
import uk.ac.ic.doc.gander.analysers.Tallies;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;

public class RunDuckHunt extends TallyRunner {

	public static void main(String[] args) throws Exception {
		new RunDuckHunt().run(args);
	}

	@Override
	protected Tallies analyse(File directory) throws Exception {

		Hierarchy hierarchy = HierarchyFactory.createHierarchy(directory);
		DuckHunt analysis = new DuckHunt(hierarchy);
		return analysis.getResult();
	}

	@Override
	protected String getCategoryTitle() {
		return "Number of types inferred for a single call target";
	}

	@Override
	protected String getTitle() {
		return "Uniqueness of duck-inferred types";
	}
}
