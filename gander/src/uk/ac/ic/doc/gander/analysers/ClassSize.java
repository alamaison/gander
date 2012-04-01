package uk.ac.ic.doc.gander.analysers;

import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.analysis.inheritance.FreshInheritanceTree;
import uk.ac.ic.doc.gander.analysis.inheritance.InheritedMethods;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.ZeroCfaTypeEngine;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.DefaultModel;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.MutableModel;

public class ClassSize {

	private final Tallies stats = new Tallies();
	private final MutableModel model;

	public ClassSize(Hierarchy hierarchy) throws Exception {
		this.model = new DefaultModel(hierarchy);
		uk.ac.ic.doc.gander.hierarchy.Package pack = hierarchy
				.getTopLevelPackage();
		analysePackage(pack);
	}

	public Tallies getResult() {
		return stats;
	}

	private void analysePackage(uk.ac.ic.doc.gander.hierarchy.Package pack)
			throws Exception {
		for (uk.ac.ic.doc.gander.hierarchy.SourceFile module : pack
				.getSourceFiles().values())
			analyseModule(module);
		for (uk.ac.ic.doc.gander.hierarchy.Package subpackage : pack
				.getPackages().values())
			analysePackage(subpackage);
	}

	private void analyseModule(
			uk.ac.ic.doc.gander.hierarchy.SourceFile hierarchyModule)
			throws Exception {
		if (hierarchyModule.isSystem())
			return;

		Module module = model.loadModule(hierarchyModule
				.getFullyQualifiedName());

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

		@Override
		public void onResolutionFailure(Class klass, exprType baseExpression) {
			System.err.println("WARNING: unable to resolve base of '"
					+ klass.getFullName() + "': " + baseExpression);
		}

		@Override
		public void onIncestuousBase(Class klass, exprType baseExpression) {
			System.err.println("WARNING: base of '" + klass.getFullName()
					+ "' resolves to itself!: " + baseExpression);
		}

	}

	private Set<String> methodsInTree(FreshInheritanceTree tree)
			throws Exception {
		return new InheritedMethods(tree, new SysErrErrorHandler())
				.methodsInTree();
	}

	private Set<String> methodsInClass(Class klass) throws Exception {
		FreshInheritanceTree tree = new FreshInheritanceTree(klass,
				new TypeResolver(new ZeroCfaTypeEngine()));
		return methodsInTree(tree);
	}
}
