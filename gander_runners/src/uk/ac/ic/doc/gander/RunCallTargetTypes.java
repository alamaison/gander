package uk.ac.ic.doc.gander;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import uk.ac.ic.doc.gander.analysers.CallTargetTypes;
import uk.ac.ic.doc.gander.calls.CallSite;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;

public class RunCallTargetTypes extends MultiProjectRunner {

	public static void main(String[] args) throws Exception {
		new RunCallTargetTypes().run(args);
	}

	@Override
	protected void analyseProject(File projectRoot) throws Exception {
		CallTargetTypes analysis = new CallTargetTypes(HierarchyFactory
				.createHierarchy(projectRoot));
		Map<CallSite, Set<PyObject>> types = analysis.getResult();
		for (Entry<CallSite, Set<PyObject>> result : types.entrySet()) {
			System.out.print(result.getKey().getScope().getFullName() + ": ");
			System.out.println(CallHelper.indirectCallName(result.getKey()
					.getCall()));
			for (PyObject type : result.getValue()) {
				System.out.println("\t" + type.getName());
			}
		}
	}
}
