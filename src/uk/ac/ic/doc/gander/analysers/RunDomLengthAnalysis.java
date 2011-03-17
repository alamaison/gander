package uk.ac.ic.doc.gander.analysers;

import java.io.File;

import uk.ac.ic.doc.gander.analysis.dominance.DominationLength;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;

public class RunDomLengthAnalysis {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		File topLevelDirectory = new File(args[0]);

		Hierarchy hierarchy = new Hierarchy(topLevelDirectory);
		DominationLength analysis = new DominationLength(hierarchy);
		
		System.out.println("==Variables matching target only==");
		System.out.println("Minimum: " + analysis.min());
		System.out.println("Maximum: " + analysis.max());
		System.out.println("Average: " + analysis.average());
		System.out.println();
	}

}
