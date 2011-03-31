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

public class ClassSizeTest {

	private static final String TEST_FOLDER = "python_test_code/class_size";
	private Hierarchy hierarchy;

	@Test
	public void singleFile() throws Throwable {
		Integer[] sizes = { 2, 7, 1 };
		check("single_file", sizes, 1, 7, 3.33);
	}

	@Test
	public void topLevel() throws Throwable {
		Integer[] sizes = { 1, 5 };
		check("top_level", sizes, 1, 5, 3);
	}

	@Test
	public void inheritance() throws Throwable {
		Integer[] sizes = { 3, 4 };
		check("inheritance", sizes, 3, 4, 3.5);
	}

	private void initialise(String caseName) throws Throwable {
		URL classSize = getClass().getResource(TEST_FOLDER);

		File classSizeDirectory = new File(classSize.toURI());
		File topLevelDirectory = new File(classSizeDirectory, caseName);

		hierarchy = new Hierarchy(topLevelDirectory);
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
		initialise(caseName);
		Tallies count = new ClassSize(hierarchy).getResult();
		assertEquals(asSortedList(expectedCounts), asSortedList(count.counts()));
		assertEquals("Incorrect min", expectedMin, count.min());
		assertEquals("Incorrect max", expectedMax, count.max());
		assertEquals("Incorrect average", expectedAverage, count.average(), 0.1);
	}

	private <T extends Comparable<? super T>> List<T> asSortedList(
			List<T> unsortedList) {
		List<T> copy = new ArrayList<T>(unsortedList);
		Collections.sort(copy);
		return copy;
	}

}
