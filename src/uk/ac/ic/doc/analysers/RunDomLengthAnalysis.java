package uk.ac.ic.doc.analysers;

import java.io.File;

import uk.ac.ic.doc.analysis.DominationLength;
import uk.ac.ic.doc.cfg.Model;

public class RunDomLengthAnalysis {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		File topLevelDirectory = new File(args[0]);

		Model model = new Model(topLevelDirectory);
		DominationLength analysis = new DominationLength(model);
		System.out.println("Minimum: " + analysis.min());
		System.out.println("Maximum: " + analysis.max());
		System.out.println("Average: " + analysis.average());
	}

}
