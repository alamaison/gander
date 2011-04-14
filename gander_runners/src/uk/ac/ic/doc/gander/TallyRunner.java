package uk.ac.ic.doc.gander;

import java.io.File;
import java.util.Set;

import uk.ac.ic.doc.gander.analysers.Tallies;

public abstract class TallyRunner extends MultiProjectRunner {

	private FrequencyPlotter plotter;

	public TallyRunner() {
		System.out.println("====" + getTitle() + " ====");
	}

	protected void analyseProject(File projectRoot) throws Exception {

		Tallies result = analyse(projectRoot);

		System.out.println("== " + projectRoot.getName() + " ==");
		System.out.println("Minimum: " + result.min());
		System.out.println("Maximum: " + result.max());
		System.out.println("Average: " + result.average());
		System.out.println("Median: " + result.median());
		printMode(result.mode());

		if (plotter == null)
			plotter = new FrequencyPlotter(getTitle(), getCategoryTitle());
		plotter.plot(result, projectRoot.getName());
	}

	private void printMode(Set<Integer> mode) {
		if (mode.size() == 1) {
			System.out.println("Mode: " + mode.iterator().next());
		} else {
			System.out.print("Modes: ");
			for (int m : mode) {
				System.out.print(m + " ");
			}
			System.out.println();
		}

	}

	protected abstract String getTitle();

	protected abstract String getCategoryTitle();

	protected abstract Tallies analyse(File projectRoot) throws Exception;
}
