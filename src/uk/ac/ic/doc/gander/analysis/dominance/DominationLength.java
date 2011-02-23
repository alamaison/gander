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
import uk.ac.ic.doc.gander.cfg.model.BasicBlock;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Package;

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
				Postdomination postdomAnalyser, Function function, Model model)
				throws Exception {

			SignatureBuilder chainAnalyser = new SignatureBuilder();

			for (BasicBlock sub : function.getCfg().getBlocks()) {
				for (Call call : new CallFinder(sub).calls()) {
					if (!isMethodCallOnName(call, function))
						continue;

					Collection<Call> dependentCalls = chainAnalyser
							.signature(extractMethodCallTarget(call), sub,
									function, model);

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

		private boolean isMethodCallOnName(Call call, Function function)
				throws Exception {
			if (!(call.func instanceof Attribute))
				return false;

			Attribute attr = (Attribute) call.func;
			if (!(attr.value instanceof Name))
				return false;

			Name variable = (Name) attr.value;

			// skip calls to module functions - they look like method calls but
			// we want to treat then differently
			TypeResolver typer = new TypeResolver(model);
			return !(typer.typeOf(variable, function) instanceof uk.ac.ic.doc.gander.flowinference.types.TModule);
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
	private Model model;

	public DominationLength(Model model) throws Exception {
		this.model = model;
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

	private void analyseClass(Class klass) throws Exception {
		for (Function method : klass.getFunctions().values())
			analyseFunction(method);
	}

	private void analyseFunction(Function function) throws Exception {
		// System.err.println("Processing " + function.getFullName());
		Domination domAnalyser = new Domination(function.getCfg());
		Postdomination postdomAnalyser = new Postdomination(function.getCfg());
		matching.analyse(domAnalyser, postdomAnalyser, function, model);
	}

}
