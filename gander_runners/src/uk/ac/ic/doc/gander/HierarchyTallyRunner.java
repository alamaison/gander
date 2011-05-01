package uk.ac.ic.doc.gander;

import java.io.File;

import uk.ac.ic.doc.gander.analysers.Tallies;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;

public abstract class HierarchyTallyRunner extends TallyRunner {

	@Override
	protected final Tallies analyse(File directory) throws Exception {
		System.out.print("Creating hierarchy... ");
		Hierarchy hierarchy = HierarchyFactory.createHierarchy(directory);
		System.out.println("done.");

		try {
			System.out.print("Starting analysis... ");
			return analyse(hierarchy);
		} finally {
			System.out.println("done.");
		}
	}

	protected abstract Tallies analyse(Hierarchy hierarchy) throws Exception;
}