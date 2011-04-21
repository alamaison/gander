package uk.ac.ic.doc.gander;

import java.io.File;

import uk.ac.ic.doc.gander.analysers.LargeClassBreakdown;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;

public class RunLargeClassBreakdown extends MultiProjectRunner {

	public static void main(String[] args) throws Exception {
		new RunLargeClassBreakdown().run(args);
	}

	@Override
	protected void analyseProject(File projectRoot) throws Exception {
		new LargeClassBreakdown(HierarchyFactory.createHierarchy(projectRoot));
	}
}
