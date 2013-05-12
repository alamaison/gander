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

		System.out.printf(
				"Proportion of method uses correctly predicted: %.2f%%\n",
				analysis.result());
	}
}
