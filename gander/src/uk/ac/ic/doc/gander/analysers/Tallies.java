/**
 * 
 */
package uk.ac.ic.doc.gander.analysers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tallies {

	private ArrayList<Integer> counts = new ArrayList<Integer>();

	public int max() {
		if (counts.size() == 0)
			return 0;
		return Collections.max(counts);
	}

	public int min() {
		if (counts.size() == 0)
			return 0;
		return Collections.min(counts);
	}

	public int median() {
		ArrayList<Integer> counts = new ArrayList<Integer>(this.counts);
		Collections.sort(counts);

		int middle = counts.size() / 2;
		if (middle % 2 == 0) {
			return (counts.get(middle) + counts.get(middle - 1)) / 2;
		} else {
			return counts.get(middle);
		}
	}

	public double average() {
		if (counts.size() == 0)
			return 0;

		double total = 0;
		for (int v : counts)
			total += v;

		return total / counts.size();
	}

	public void addTally(int count) {
		counts.add(count);
	}

	public List<Integer> counts() {
		return counts;
	}
}