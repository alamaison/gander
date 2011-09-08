package uk.ac.ic.doc.gander.analysers;

import java.util.Set;

import uk.ac.ic.doc.gander.analysis.inheritance.FreshInheritanceTree;
import uk.ac.ic.doc.gander.analysis.inheritance.InheritedMethods;
import uk.ac.ic.doc.gander.analysis.inheritance.Node;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.MutableModel;

public class LargeClassBreakdown {

	private MutableModel model;
	private TypeResolver resolver;

	public LargeClassBreakdown(Hierarchy hierarchy) throws Exception {
		this.model = new MutableModel(hierarchy);
		this.resolver = new TypeResolver(model);
		uk.ac.ic.doc.gander.hierarchy.Package pack = hierarchy
				.getTopLevelPackage();
		analysePackage(pack);
	}

	private void analysePackage(uk.ac.ic.doc.gander.hierarchy.Package pack)
			throws Exception {
		for (uk.ac.ic.doc.gander.hierarchy.SourceFile module : pack.getSourceFiles()
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
		FreshInheritanceTree tree = new FreshInheritanceTree(klass, resolver);
		int size = measureTree(tree);
		if (size > 50) {
			System.out.println("Class size " + size);
			printTree(tree);
		}
	}

	private void printTree(FreshInheritanceTree tree) {
		Node node = tree.getTree();
		System.out.println("Methods of " + node.getKlass().getFullName() + ":");
		printTree(node, 1);
	}

	private void printTree(Node node, int indentCount) {
		printClass(node.getKlass(), indentCount);
		for (Node baseNode : node.getBases()) {
			if (baseNode == null) {
				System.out.println(indent(indentCount)
						+ "[Inherits from unresolved base]");
			} else {
				System.out.println(indent(indentCount) + "[Inherits from "
						+ baseNode.getKlass().getFullName() + "]:");
				printTree(baseNode, indentCount + 1);
			}
		}
	}

	private void printClass(Class klass, int indentCount) {

		for (String methodName : klass.getFunctions().keySet()) {
			System.out.println(indent(indentCount) + methodName);
		}
	}

	private String indent(int stops) {
		String s = "";
		while (stops-- > 0) {
			s += "\t";
		}
		return s;
	}

	private int measureTree(FreshInheritanceTree tree) throws Exception {
		int count = 0;
		for (String name : methodsInTree(tree)) {
			if (!name.startsWith("_"))
				++count;
		}
		return count;
	}

	private Set<String> methodsInTree(FreshInheritanceTree tree)
			throws Exception {
		return new InheritedMethods(tree).methodsInTree();
	}
}
