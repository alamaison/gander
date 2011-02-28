package uk.ac.ic.doc.gander.analysers;

import java.io.File;

import uk.ac.ic.doc.gander.analysis.dominance.DominationLength;
import uk.ac.ic.doc.gander.model.Model;

public class RunDomLengthAnalysis {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		File topLevelDirectory = new File(args[0]);

		Model model = new Model(topLevelDirectory);
		DominationLength analysis = new DominationLength(model);
		
		System.out.println("==Variables matching target only==");
		System.out.println("Minimum: " + analysis.min());
		System.out.println("Maximum: " + analysis.max());
		System.out.println("Average: " + analysis.average());
		System.out.println();
	}

}
