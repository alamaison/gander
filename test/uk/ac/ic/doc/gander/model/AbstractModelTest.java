package uk.ac.ic.doc.gander.model;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

public abstract class AbstractModelTest {

	private Model model = null;

	protected Model getModel() {
		return model;
	}

	protected static final String PACKAGE_STRUCTURE_PROJ = "python_test_code/package_structure";

	protected static final String MODULE_STRUCTURE_PROJ = "python_test_code/model_structure";

	protected void createTestModel(String projectPath) throws Throwable {
		URL topLevel = getClass().getResource(projectPath);

		File topLevelDirectory = new File(topLevel.toURI());

		model = new Model(topLevelDirectory);
	}

	protected static <T> void assertKeys(Map<String, T> keys, String... expected) {
		assertEquals(new HashSet<String>(Arrays.asList(expected)), keys
				.keySet());
	}
}
