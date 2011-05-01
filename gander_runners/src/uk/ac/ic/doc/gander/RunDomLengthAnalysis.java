package uk.ac.ic.doc.gander;

import uk.ac.ic.doc.gander.analysers.DominationLength;
import uk.ac.ic.doc.gander.analysers.Tallies;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;

public class RunDomLengthAnalysis extends HierarchyTallyRunner {

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
	protected Tallies analyse(Hierarchy hierarchy) throws Exception {
		DominationLength analysis = new DominationLength(hierarchy);
		return analysis.getResult();
	}

}
