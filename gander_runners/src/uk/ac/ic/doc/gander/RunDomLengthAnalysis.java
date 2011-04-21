package uk.ac.ic.doc.gander;

import java.io.File;

import uk.ac.ic.doc.gander.analysers.DominationLength;
import uk.ac.ic.doc.gander.analysers.Tallies;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;

public class RunDomLengthAnalysis extends TallyRunner {

	public static void main(String[] args) throws Exception {
		new RunDomLengthAnalysis().run(args);
	}

	@Override
	protected String getTitle() {
		return "Signature size for method call targets";
	}

	@Override
	protected String getCategoryTitle() {
		return "Signature size";
	}

	@Override
	protected Tallies analyse(File directory) throws Exception {
		Hierarchy hierarchy = HierarchyFactory.createHierarchy(directory);
		DominationLength analysis = new DominationLength(hierarchy);
		return analysis.getResult();
	}

}
