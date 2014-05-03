package uk.ac.ic.doc.gander.hierarchy;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Before;

public abstract class AbstractHierarchyTest {

	private Hierarchy hierarchy = null;

	protected Hierarchy getHierarchy() {
		return hierarchy;
	}

	protected static final String TEST_PROJ = "python_test_code";
	protected static final String STDLIB_PROJ = "dummy_standard_lib";

	@Before
	public void createTestModel() throws Throwable {
		URL topLevel = getClass().getResource(TEST_PROJ);
		URL stdLib = getClass().getResource(STDLIB_PROJ);

		File topLevelDirectory = new File(topLevel.toURI());
		File stdlib = new File(stdLib.toURI());

		List<File> dirs = new ArrayList<File>();
		dirs.add(topLevelDirectory);
		
		List<File> stdLibDirs = new ArrayList<File>();
		stdLibDirs.add(stdlib);
		stdLibDirs.add(new File("nonexistentdir"));
		hierarchy = HierarchyFactory.createHierarchy(dirs, stdLibDirs);
	}

	protected static <T> void assertKeys(Map<String, T> keys, String... expected) {
		assertEquals(new HashSet<String>(Arrays.asList(expected)), keys
				.keySet());
	}
}
