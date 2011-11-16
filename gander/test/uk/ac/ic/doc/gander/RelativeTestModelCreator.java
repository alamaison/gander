package uk.ac.ic.doc.gander;

import java.io.File;
import java.net.URL;

import junit.framework.AssertionFailedError;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;
import uk.ac.ic.doc.gander.model.DefaultModel;
import uk.ac.ic.doc.gander.model.MutableModel;

/**
 * Load model for test directory specified relative to the test's class file
 * location on disk.
 */
public final class RelativeTestModelCreator {
	private final MutableModel model;

	public RelativeTestModelCreator(String relativePath, Object klass) {
		try {
			final URL topLevel = klass.getClass().getResource(relativePath);
			final Hierarchy hierarchy = HierarchyFactory
					.createHierarchy(new File(topLevel.toURI()));
			model = new DefaultModel(hierarchy);
		} catch (Exception e) {
			throw new AssertionFailedError(
					"Exception while creating test model: " + e);
		}
	}

	public MutableModel getModel() {
		return model;
	}

}