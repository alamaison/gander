package uk.ac.ic.doc.gander.analysers;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;

public class CodeCompletionPredictionTest {

	private static final String TEST_FOLDER = "python_test_code/code_completion";
	private Hierarchy hierarchy;
	private File topLevel;

	@Test
	public void basic() throws Throwable {
		setup("basic");
		float successRate = new CodeCompletionPrediction(hierarchy, topLevel)
				.result();
		assertEquals(50.0, successRate, 0.001);
	}

	public void setup(String caseName) throws Throwable {
		URL testFolder = getClass().getResource(TEST_FOLDER);
		topLevel = new File(new File(testFolder.toURI()), caseName);

		hierarchy = HierarchyFactory.createHierarchy(topLevel);
	}

}
