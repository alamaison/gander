package uk.ac.ic.doc.gander;

import java.io.File;

import uk.ac.ic.doc.gander.analysers.CodeCompletionPrediction;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;

public final class RunCodeCompletionPrediction extends MultiProjectRunner {

	public static void main(String[] args) throws Exception {
		new RunCodeCompletionPrediction().run(args);
	}

	@Override
	protected void analyseProject(File projectRoot) throws Exception {
		new RunCodeCompletionPredictionAnalyser(projectRoot);
	}
}

final class RunCodeCompletionPredictionAnalyser {

	RunCodeCompletionPredictionAnalyser(File projectRoot) throws Exception {
		CodeCompletionPrediction analysis = new CodeCompletionPrediction(
				HierarchyFactory.createHierarchy(projectRoot), projectRoot);

		System.out.println("Proportion of method uses correctly predicted");
		System.out.printf("  Interface recovery %.2f%%\n",
				analysis.interfaceResult());
		System.out.printf("  Flow analysis %.2f%%\n", analysis.flowResult());
		System.out.printf("  Contraindication %.2f%%\n",
				analysis.contraindicationResult());
	}
}
