package uk.ac.ic.doc.gander.analysers;

import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.analysis.inheritance.InheritanceTree;
import uk.ac.ic.doc.gander.analysis.inheritance.InheritedMethods;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;

public class ClassSize {

	private Tallies stats = new Tallies();
	private Model model;

	public ClassSize(Hierarchy hierarchy) throws Exception {
		this.model = new Model(hierarchy);
		uk.ac.ic.doc.gander.hierarchy.Package pack = hierarchy
				.getTopLevelPackage();
		analysePackage(pack);
	}

	public Tallies getResult() {
		return stats;
	}

	private void analysePackage(uk.ac.ic.doc.gander.hierarchy.Package pack)
			throws Exception {
		for (uk.ac.ic.doc.gander.hierarchy.Module module : pack.getModules()
				.values())
			analyseModule(module.getFullyQualifiedName());
		for (uk.ac.ic.doc.gander.hierarchy.Package subpackage : pack
				.getPackages().values())
			analysePackage(subpackage);
	}

	private void analyseModule(String moduleName) throws Exception {
		Module module = model.loadModule(moduleName);

		for (Class klass : module.getClasses().values())
			analyseClass(klass);
	}

	private void analyseClass(Class klass) throws Exception {
		stats.addTally(measureClass(klass));
	}

	private int measureClass(Class klass) throws Exception {
		int count = 0;
		for (String name : methodsInClass(klass)) {
			if (!name.startsWith("_"))
				++count;
		}
		return count;
	}

	private class SysErrErrorHandler implements InheritedMethods.ErrorHandler {

		public void onResolutionFailure(Class klass, exprType baseExpression) {
			System.err.println("WARNING: unable to resolve base of '"
					+ klass.getFullName() + "': " + baseExpression);
		}

		public void onIncestuousBase(Class klass, exprType baseExpression) {
			System.err.println("WARNING: base of '" + klass.getFullName()
					+ "' resolves to itself!: " + baseExpression);
		}

	}

	private Set<String> methodsInTree(InheritanceTree tree) throws Exception {
		return new InheritedMethods(tree, new SysErrErrorHandler())
				.methodsInTree();
	}

	private Set<String> methodsInClass(Class klass) throws Exception {
		InheritanceTree tree = new InheritanceTree(klass, model);
		return methodsInTree(tree);
	}
}
