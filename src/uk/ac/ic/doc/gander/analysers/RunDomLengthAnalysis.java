package uk.ac.ic.doc.gander.analysers;

import java.io.File;

import uk.ac.ic.doc.gander.analysis.dominance.DominationLength;
import uk.ac.ic.doc.gander.cfg.Model;

public class RunDomLengthAnalysis {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		File topLevelDirectory = new File(args[0]);

		Model model = new Model(topLevelDirectory);
		DominationLength analysis = new DominationLength(model);
		
		System.out.println("==Considering all statements==");
		System.out.println("Minimum: " + analysis.all.min());
		System.out.println("Maximum: " + analysis.all.max());
		System.out.println("Average: " + analysis.all.average());
		System.out.println();
		
		System.out.println("==Considering only variables==");
		System.out.println("Minimum: " + analysis.variableOnly.min());
		System.out.println("Maximum: " + analysis.variableOnly.max());
		System.out.println("Average: " + analysis.variableOnly.average());
		System.out.println();
		
		System.out.println("==Variables matching target only==");
		System.out.println("Minimum: " + analysis.matching.min());
		System.out.println("Maximum: " + analysis.matching.max());
		System.out.println("Average: " + analysis.matching.average());
		System.out.println();
	}

}
