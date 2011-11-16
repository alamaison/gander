package uk.ac.ic.doc.gander.model;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;

public abstract class AbstractModelTest {

	private MutableModel model = null;

	protected MutableModel getModel() {
		return model;
	}

	protected static final String PACKAGE_STRUCTURE_PROJ = "python_test_code/package_structure";
	protected static final String MODULE_STRUCTURE_PROJ = "python_test_code/model_structure";
	protected static final String IMPORTING_PROJ = "python_test_code/importing";

	private static final String[] EXPECTED_BUILTIN_MODULES = { "types" };

	protected void createTestModel(String projectPath) throws Throwable {
		URL topLevel = getClass().getResource(projectPath);

		File topLevelDirectory = new File(topLevel.toURI());

		Hierarchy hierarchy = HierarchyFactory
				.createHierarchy(topLevelDirectory);
		model = new DefaultModel(hierarchy);
	}

	protected static <T> void assertKeys(String message, Map<String, T> keys,
			String... expected) {
		assertEquals(message, new HashSet<String>(Arrays.asList(expected)),
				keys.keySet());
	}

	protected static <T> void assertKeys(Map<String, T> keys,
			String... expected) {
		assertEquals(new HashSet<String>(Arrays.asList(expected)), keys
				.keySet());
	}

	protected final String[] addToBuiltins(String... expected) {
		String[] result = new String[expected.length
				+ EXPECTED_BUILTIN_MODULES.length];
		System.arraycopy(expected, 0, result, 0, expected.length);
		System.arraycopy(EXPECTED_BUILTIN_MODULES, 0, result, expected.length,
				EXPECTED_BUILTIN_MODULES.length);

		return result;
	}
}
