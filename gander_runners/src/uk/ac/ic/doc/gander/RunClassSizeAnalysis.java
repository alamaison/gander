package uk.ac.ic.doc.gander;

import java.io.File;

import uk.ac.ic.doc.gander.analysers.ClassSize;
import uk.ac.ic.doc.gander.analysers.Tallies;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;

public class RunClassSizeAnalysis extends TallyRunner {

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
	protected Tallies analyse(File directory) throws Exception {
		Hierarchy hierarchy = HierarchyFactory.createHierarchy(directory);
		ClassSize analysis = new ClassSize(hierarchy);
		return analysis.getResult();
	}

}
