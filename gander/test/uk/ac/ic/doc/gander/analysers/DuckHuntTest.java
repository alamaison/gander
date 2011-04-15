package uk.ac.ic.doc.gander.analysers;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;

public class DuckHuntTest {

	private static final String TEST_FOLDER = "python_test_code/duck_hunt";
	private Hierarchy hierarchy;

	@Test
	public void infile() throws Throwable {

		Integer[] counts = { 2, 2, 2, 1, 1 };
		check("infile", counts, 1, 2, 1.6);
	}

	private void check(String caseName, Integer[] expectedCounts,
			int expectedMin, int expectedMax, double expectedAverage)
			throws Throwable {
		check(caseName, Arrays.asList(expectedCounts), expectedMin,
				expectedMax, expectedAverage);
	}

	private void check(String caseName, List<Integer> expectedCounts,
			int expectedMin, int expectedMax, double expectedAverage)
			throws Throwable {
		setup(caseName);
		Tallies count = new DuckHunt(hierarchy).getResult();
		assertEquals(asSortedList(expectedCounts), asSortedList(count.counts()));
		assertEquals("Incorrect min", expectedMin, count.min());
		assertEquals("Incorrect max", expectedMax, count.max());
		assertEquals("Incorrect average", expectedAverage, count.average(), 0.1);
	}

	public void setup(String caseName) throws Throwable {
		URL testFolder = getClass().getResource(TEST_FOLDER);
		File topLevel = new File(new File(testFolder.toURI()), caseName);

		hierarchy = HierarchyFactory.createHierarchyNoLibrary(topLevel);
	}

	private <T extends Comparable<? super T>> List<T> asSortedList(
			List<T> unsortedList) {
		List<T> copy = new ArrayList<T>(unsortedList);
		Collections.sort(copy);
		return copy;
	}

}
