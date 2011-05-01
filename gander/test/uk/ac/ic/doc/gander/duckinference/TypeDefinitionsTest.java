package uk.ac.ic.doc.gander.duckinference;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.MutableModel;
import uk.ac.ic.doc.gander.model.Module;

public class TypeDefinitionsTest {

	private static final String TEST_FOLDER = "python_test_code";
	private MutableModel model;
	private Hierarchy hierarchy;

	public void setup(String caseName) throws Throwable {
		URL testFolder = getClass().getResource(TEST_FOLDER);
		File topLevel = new File(new File(testFolder.toURI()), caseName);

		hierarchy = HierarchyFactory.createHierarchy(topLevel);
		model = new MutableModel(hierarchy);
	}

	@Test
	public void infileSingle() throws Throwable {
		setup("infile_single");

		Module start = model.loadModule("start");

		Class expected[] = { start.getClasses().get("A"),
				start.getClasses().get("B"), start.getClasses().get("C") };

		assertCollectedClasses(expected);
	}

	@Test
	public void inherited() throws Throwable {
		setup("inherited");

		Module start = model.loadModule("start");

		Class expected[] = { start.getClasses().get("A"),
				start.getClasses().get("B"), start.getClasses().get("C"),
				start.getClasses().get("Base") };

		assertCollectedClasses(expected);
	}

	private void assertCollectedClasses(Class[] specifiedExpected) {
		Collection<Class> builtins = model.getTopLevelPackage().getClasses()
				.values();
		Set<Class> expected = new HashSet<Class>(Arrays
				.asList(specifiedExpected));
		expected.addAll(builtins);

		assertEquals("Types collected don't match expected classes", expected,
				new HashSet<Class>(new LoadedTypeDefinitions(model)
						.getDefinitions()));
	}
}
