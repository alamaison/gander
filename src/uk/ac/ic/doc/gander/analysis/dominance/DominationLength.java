package uk.ac.ic.doc.gander.analysis.dominance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;

import uk.ac.ic.doc.gander.analysis.BasicBlockTraverser;
import uk.ac.ic.doc.gander.analysis.SignatureBuilder;
import uk.ac.ic.doc.gander.cfg.Model;
import uk.ac.ic.doc.gander.cfg.model.BasicBlock;
import uk.ac.ic.doc.gander.cfg.model.Cfg;
import uk.ac.ic.doc.gander.cfg.model.Class;
import uk.ac.ic.doc.gander.cfg.model.Function;
import uk.ac.ic.doc.gander.cfg.model.Method;
import uk.ac.ic.doc.gander.cfg.model.Module;
import uk.ac.ic.doc.gander.cfg.model.Package;
import uk.ac.ic.doc.gander.flowinference.types.TypeResolutionVisitor;

public class DominationLength {

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

	private static Name extractMethodCallTarget(Call call) {
		Attribute fieldAccess = (Attribute) call.func;
		return (Name) fieldAccess.value;
	}

	private static NameTok extractMethodCallName(Call call) {
		Attribute fieldAccess = (Attribute) call.func;
		return (NameTok) fieldAccess.attr;
	}

	public class SameVariableOnlyAnalysis extends AbstractAnalysis {

		private class CallFinder extends BasicBlockTraverser {

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
		}

		public void analyse(Domination domAnalyser,
				Postdomination postdomAnalyser, Module module, Cfg graph)
				throws Exception {

			SignatureBuilder chainAnalyser = new SignatureBuilder();

			for (BasicBlock sub : graph.getBlocks()) {
				for (Call call : new CallFinder(sub).calls()) {
					if (!isMethodCallOnName(call, module))
						continue;

					Collection<Call> dependentCalls = chainAnalyser.signature(
							extractMethodCallTarget(call), sub, module, graph);

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

		private void printChain(int count, Call call,
				Collection<Call> dependentCalls) {
			if (dependentCalls != null) {
				System.err.println("Chain length: " + count);
				System.err.println("'" + call + "' chain:\n" + dependentCalls
						+ "\n\n");
			}
		}

		private boolean isMethodCallOnName(Call call, Module module)
				throws Exception {
			if (!(call.func instanceof Attribute))
				return false;

			Attribute attr = (Attribute) call.func;
			if (!(attr.value instanceof Name))
				return false;

			Name variable = (Name) attr.value;

			// skip calls to module functions - they look like method calls but
			// we want to treat then differently
			TypeResolutionVisitor typer = new TypeResolutionVisitor(module
					.getAst());
			return !(typer.typeOf(variable.id) instanceof uk.ac.ic.doc.gander.flowinference.types.Module);
		}

		private int countUniqueMethodNames(Iterable<Call> calls) {
			Set<String> methods = new HashSet<String>();
			for (Call call : calls) {
				methods.add(extractMethodCallName(call).id);
			}
			return methods.size();
		}

	}

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
		matching.analyse(domAnalyser, postdomAnalyser, module, graph);
	}

}
