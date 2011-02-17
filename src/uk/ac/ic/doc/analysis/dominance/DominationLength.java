package uk.ac.ic.doc.analysis.dominance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.AugAssign;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.Num;
import org.python.pydev.parser.jython.ast.Str;
import org.python.pydev.parser.jython.ast.Subscript;
import org.python.pydev.parser.jython.ast.VisitorBase;

import uk.ac.ic.doc.analysis.BasicBlockVisitor;
import uk.ac.ic.doc.analysis.DependenceChain;
import uk.ac.ic.doc.cfg.Model;
import uk.ac.ic.doc.cfg.model.BasicBlock;
import uk.ac.ic.doc.cfg.model.Cfg;
import uk.ac.ic.doc.cfg.model.Class;
import uk.ac.ic.doc.cfg.model.Function;
import uk.ac.ic.doc.cfg.model.Method;
import uk.ac.ic.doc.cfg.model.Module;
import uk.ac.ic.doc.cfg.model.Package;

public class DominationLength {

	public class StatementFilter extends VisitorBase {

		private Set<SimpleNode> statements = new HashSet<SimpleNode>();

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {
			System.err.println("unhandled node: " + node.toString());
			return null;
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			// Don't traverse nodes
		}

		@Override
		public Object visitNameTok(NameTok node) throws Exception {
			return statements.add(node);
		}

		@Override
		public Object visitAssign(Assign node) throws Exception {
			return statements.add(node);
		}

		@Override
		public Object visitAugAssign(AugAssign node) throws Exception {
			return statements.add(node);
		}

		@Override
		public Object visitAttribute(Attribute node) throws Exception {
			return statements.add(node);
		}

		@Override
		public Object visitSubscript(Subscript node) throws Exception {
			return statements.add(node);
		}

		@Override
		public Object visitCall(Call node) throws Exception {
			return statements.add(node);
		}

		@Override
		public Object visitNum(Num node) throws Exception {
			return null;
		}

		@Override
		public Object visitName(Name node) throws Exception {
			return statements.add(node);
		}

		@Override
		public Object visitStr(Str node) throws Exception {
			return null;
		}

		public Set<SimpleNode> statements() {
			return statements;
		}

	}

	public class ControlDependence {

		private Map<BasicBlock, Set<SimpleNode>> dependentStatements = new HashMap<BasicBlock, Set<SimpleNode>>();

		public ControlDependence(Domination domAnalyser,
				Postdomination postdomAnalyser, Cfg graph) {

			for (BasicBlock subject : graph.getBlocks()) {
				Set<SimpleNode> controlDependentStatements = new HashSet<SimpleNode>();

				for (BasicBlock dominators : domAnalyser.dominators(subject))
					controlDependentStatements.addAll(dominators.statements);

				for (BasicBlock postdominators : postdomAnalyser
						.dominators(subject))
					controlDependentStatements
							.addAll(postdominators.statements);

				dependentStatements.put(subject, controlDependentStatements);
			}
		}

		public Set<SimpleNode> dependentStatements(BasicBlock block) {
			return dependentStatements.get(block);
		}

	}

	public abstract class AbstractAnalysis {

		private ArrayList<Integer> counts = new ArrayList<Integer>();

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

		public void add(int count) {
			counts.add(count);
		}
	}

	public class AllStatementAnalysis extends AbstractAnalysis {

		public void analyse(Domination domAnalyser,
				Postdomination postdomAnalyser, Cfg graph) {

			ControlDependence dependence = new ControlDependence(domAnalyser,
					postdomAnalyser, graph);

			for (BasicBlock sub : graph.getBlocks()) {
				Set<SimpleNode> controlDependentStatements = dependence
						.dependentStatements(sub);

				for (int i = 0; i < sub.statements.size(); i++)
					add(new Integer(controlDependentStatements.size()));
			}
		}
	}

	public class VariableOnlyAnalysis extends AbstractAnalysis {
		public void analyse(Domination domAnalyser,
				Postdomination postdomAnalyser, Cfg graph) throws Exception {

			ControlDependence dependence = new ControlDependence(domAnalyser,
					postdomAnalyser, graph);

			for (BasicBlock sub : graph.getBlocks()) {
				Set<SimpleNode> controlDependentStatements = dependence
						.dependentStatements(sub);
				Set<SimpleNode> filteredStatements = filterStatements(controlDependentStatements);

				for (int i = 0; i < sub.statements.size(); i++)
					add(new Integer(filteredStatements.size()));
			}
		}

		private Set<SimpleNode> filterStatements(Set<SimpleNode> statements)
				throws Exception {

			StatementFilter statementFilter = new StatementFilter();
			for (SimpleNode node : statements)
				node.accept(statementFilter);

			return statementFilter.statements();
		}

	}

	static NameTok extractMethodCallName(Call call) {
		Attribute fieldAccess = (Attribute) call.func;
		return (NameTok) fieldAccess.attr;
	}

	public class SameVariableOnlyAnalysis extends AbstractAnalysis {

		private class CallFinder extends BasicBlockVisitor {

			private ArrayList<Call> calls = new ArrayList<Call>();

			public CallFinder(BasicBlock block) throws Exception {
				for (SimpleNode statement : block)
					statement.accept(this);
			}

			@Override
			public Object visitCall(Call node) throws Exception {
				if (node.func instanceof Attribute)
					calls.add(node);
				return null;
			}

			Iterable<Call> calls() {
				return calls;
			}

			@Override
			protected Object unhandled_node(SimpleNode node) throws Exception {
				return null;
			}

			@Override
			public void traverse(SimpleNode node) throws Exception {
				node.traverse(this);
			}

		}

		public void analyse(Domination domAnalyser,
				Postdomination postdomAnalyser, Module module, Cfg graph)
				throws Exception {

			DependenceChain chainAnalyser = new DependenceChain(module, graph);

			for (BasicBlock sub : graph.getBlocks()) {
				for (Call call : new CallFinder(sub).calls()) {
					if (!isMethodCallOnName(call))
						continue;

					Collection<Call> dependentCalls = chainAnalyser
							.dependentCalls(call, sub);

					if (dependentCalls != null) {
						int count = countUniqueMethodNames(dependentCalls);
						// if (count > 1)
						// printChain(call, dependentCalls);

						add(new Integer(count));
					}
					// If no dependent calls, this isn't a chain length of 0.
					// It means the call wasn't a method call at all! Something
					// like a function being called on a module rather than
					// an object method.
				}
			}
		}

		private void printChain(Call call, Collection<Call> dependentCalls) {
			if (dependentCalls != null) {
				System.err.println("'" + call + "' chain:\n" + dependentCalls
						+ "\n\n");
			}
		}

		private boolean isMethodCallOnName(Call call) {
			if (!(call.func instanceof Attribute))
				return false;

			Attribute attr = (Attribute) call.func;
			return attr.value instanceof Name;
		}

		private int countUniqueMethodNames(Iterable<Call> calls) {
			Set<String> methods = new HashSet<String>();
			for (Call call : calls) {
				methods.add(extractMethodCallName(call).id);
			}
			return methods.size();
		}

	}

	public AllStatementAnalysis all = new AllStatementAnalysis();
	public VariableOnlyAnalysis variableOnly = new VariableOnlyAnalysis();
	public SameVariableOnlyAnalysis matching = new SameVariableOnlyAnalysis();

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
			analyseFunction(module, function);

		for (Class klass : module.getClasses().values())
			analyseClass(module, klass);
	}

	private void analyseFunction(Module module, Function function)
			throws Exception {
		//System.err.println("Processing " + function.getFullName());
		Cfg graph = function.getCfg();
		analyseChainSize(module, graph);
	}

	private void analyseClass(Module module, Class klass) throws Exception {
		for (Method method : klass.getMethods().values())
			analyseFunction(module, method);
	}

	private void analyseChainSize(Module module, Cfg graph) throws Exception {
		Domination domAnalyser = new Domination(graph);
		Postdomination postdomAnalyser = new Postdomination(graph);
		all.analyse(domAnalyser, postdomAnalyser, graph);
		variableOnly.analyse(domAnalyser, postdomAnalyser, graph);
		matching.analyse(domAnalyser, postdomAnalyser, module, graph);
	}

}
