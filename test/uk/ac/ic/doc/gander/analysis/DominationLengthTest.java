package uk.ac.ic.doc.gander.analysis;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import uk.ac.ic.doc.gander.analysis.dominance.DominationLength;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;

public class DominationLengthTest {

	private static final String TEST_FOLDER = "python_test_code/dom_length";
	private Hierarchy hierarchy;

	@Test
	public void inline() throws Throwable {
		Integer[] counts = { 2, 2, 3, 1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 2 };
		check("inline", counts, 1, 3, 1.55);
	}

	@Test
	public void local() throws Throwable {
		Integer[] counts = { 4, 4, 2, 2, 3 };
		check("local", counts, 2, 4, 3);
	}

	@Test
	public void siblingImport() throws Throwable {
		Integer[] counts = { 3, 3, 4, 2, 1, 2, 1, 4 };
		check("sibling_import", counts, 1, 4, 2.5);
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
		DominationLength counter = new DominationLength(hierarchy);
		assertEquals(asSortedList(expectedCounts), asSortedList(counter
				.counts()));
		assertEquals("Incorrect min", expectedMin, counter.min());
		assertEquals("Incorrect max", expectedMax, counter.max());
		assertEquals("Incorrect average", expectedAverage, counter.average(),
				0.1);
	}

	private <T extends Comparable<? super T>> List<T> asSortedList(
			List<T> unsortedList) {
		List<T> copy = new ArrayList<T>(unsortedList);
		Collections.sort(copy);
		return copy;
	}

	private void initialise(String caseName) throws Throwable {
		URL domLength = getClass().getResource(TEST_FOLDER);

		File domLengthDirectory = new File(domLength.toURI());
		File topLevelDirectory = new File(domLengthDirectory, caseName);

		hierarchy = new Hierarchy(topLevelDirectory);
	}

}
