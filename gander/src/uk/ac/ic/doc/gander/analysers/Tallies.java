/**
 * 
 */
package uk.ac.ic.doc.gander.analysers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
	
	public Set<Integer> mode() {
		Map<Integer, Integer> occurrences = new HashMap<Integer, Integer>();
		for (Integer c : counts) {
			Integer m = occurrences.get(c);
			if (m == null) {
				m = new Integer(0);
			}
			
			occurrences.put(c, m + 1);
		}
		
		int maximumOccurrence = Collections.max(occurrences.values());
		
		Set<Integer> modes = new HashSet<Integer>();
		for (Entry<Integer, Integer> e : occurrences.entrySet()) {
			if (e.getValue() == maximumOccurrence)
				modes.add(e.getKey());
		}
		
		return modes;
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