package uk.ac.ic.doc.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;

import uk.ac.ic.doc.cfg.Domination;
import uk.ac.ic.doc.cfg.Model;
import uk.ac.ic.doc.cfg.Postdomination;
import uk.ac.ic.doc.cfg.model.BasicBlock;
import uk.ac.ic.doc.cfg.model.Cfg;
import uk.ac.ic.doc.cfg.model.Class;
import uk.ac.ic.doc.cfg.model.Function;
import uk.ac.ic.doc.cfg.model.Method;
import uk.ac.ic.doc.cfg.model.Module;
import uk.ac.ic.doc.cfg.model.Package;

public class DominationLength {

	private ArrayList<Integer> counts = new ArrayList<Integer>();

	public DominationLength(Model model) throws Exception {
		Package pack = model.getTopLevelPackage();
		analysePackage(pack);
	}

	private void analysePackage(Package pack) throws Exception {
		for (Module module : pack.getModules().values())
			analyseModule(module);
		for (Package subpackage : pack.getPackages().values())
			analysePackage(subpackage);
	}

	private void analyseModule(Module module) throws Exception {
		for (Function function : module.getFunctions().values())
			analyseFunction(function);

		for (Class klass : module.getClasses().values())
			analyseClass(klass);
	}

	private void analyseFunction(Function function) throws Exception {
		System.err.println("Processing " + function.getFullName());
		Cfg graph = function.getCfg();
		analyseChainSize(graph);
	}

	private void analyseClass(Class klass) throws Exception {
		for (Method method : klass.getMethods().values())
			analyseFunction(method);
	}

	private void analyseChainSize(Cfg graph) {
		Domination domAnalyser = new Domination(graph.getBlocks(),
				graph.getStart());
		Postdomination postdomAnalyser = new Postdomination(graph.getBlocks(),
				graph.getEnd());

		for (BasicBlock sub : graph.getBlocks()) {
			Set<SimpleNode> statements = new HashSet<SimpleNode>();

			for (BasicBlock dom : domAnalyser.dominators(sub))
				statements.addAll(dom.statements);

			for (BasicBlock dom : postdomAnalyser.dominators(sub))
				statements.addAll(dom.statements);

			for (int i = 0; i < sub.statements.size(); i++)
				counts.add(new Integer(statements.size()));
		}
	}

	public int max() {
		if (counts.size() == 0)
			return 0;
		return Collections.max(counts);
	}

	public int min() {
		if (counts.size() == 0)
			return 0;
		return Collections.min(counts);
	}

	public double average() {
		if (counts.size() == 0)
			return 0;

		double total = 0;
		for (int v : counts)
			total += v;

		return total / counts.size();
	}

	public int expressionCount() {
		return counts.size();
	}

}
