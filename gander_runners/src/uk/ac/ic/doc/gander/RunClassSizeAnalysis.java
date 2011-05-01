package uk.ac.ic.doc.gander;

import uk.ac.ic.doc.gander.analysers.ClassSize;
import uk.ac.ic.doc.gander.analysers.Tallies;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;

public class RunClassSizeAnalysis extends HierarchyTallyRunner {

	public static void main(String[] args) throws Exception {
		new RunClassSizeAnalysis().run(args);
	}

	@Override
	protected String getTitle() {
		return "Profile of class sizes as number of public methods";
	}

	@Override
	protected String getCategoryTitle() {
		return "Class size";
	}

	@Override
	protected Tallies analyse(Hierarchy hierarchy) throws Exception {
		ClassSize analysis = new ClassSize(hierarchy);
		return analysis.getResult();
	}

}
