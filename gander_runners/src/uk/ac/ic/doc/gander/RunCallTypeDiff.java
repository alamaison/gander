package uk.ac.ic.doc.gander;

import java.io.File;
import java.util.Set;

import uk.ac.ic.doc.gander.analysers.CallTargetTypeDiff;
import uk.ac.ic.doc.gander.analysers.CallTargetTypeDiff.DiffResult;
import uk.ac.ic.doc.gander.analysers.CallTargetTypeDiff.ResultObserver;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;

public final class RunCallTypeDiff extends MultiProjectRunner {

	public static void main(String[] args) throws Exception {
		new RunCallTypeDiff().run(args);
	}

	@Override
	protected void analyseProject(File projectRoot) throws Exception {
		new RunCallTypeDiffProjectAnalyser(projectRoot);
	}
}

final class DiffPrinter {

	public DiffPrinter(DiffResult result) {
		System.out.print(result.callSite().getScope().getFullName() + ": ");
		System.out.println(result.callSite().getCall());

		if (!result.resultsMatch()) {
			System.out.println("DUCKING TYPES:");
			System.out.println("\t" + result.duckType());
			System.out.println("FLOW TYPES:");
			System.out.println("\t" + result.flowType());

			if (result.resultsAreDisjoint()) {
				System.err.println("DISJOINT TYPES");
			}
		} else {
			System.out.println("**TYPES MATCH**");
			System.out.println("\t" + result.duckType());
		}
	}
}

final class DiffObserver implements ResultObserver {

	public void resultReady(DiffResult result) {
		new DiffPrinter(result);
	}

}

final class RunCallTypeDiffProjectAnalyser {

	RunCallTypeDiffProjectAnalyser(File projectRoot) throws Exception {
		CallTargetTypeDiff analysis = new CallTargetTypeDiff(
				HierarchyFactory.createHierarchy(projectRoot), projectRoot,
				new DiffObserver(), new SqlLiteDumper());

		Set<DiffResult> duckTypes = analysis.result();

		for (DiffResult result : duckTypes) {

			new DiffPrinter(result);
		}
	}
}
