package uk.ac.ic.doc.gander.analysers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.gander.MethodCallHelper;
import uk.ac.ic.doc.gander.analysis.BasicBlockTraverser;
import uk.ac.ic.doc.gander.analysis.signatures.SignatureBuilder;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyWalker;
import uk.ac.ic.doc.gander.hierarchy.Package;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;

public class DominationLength extends HierarchyWalker {

	private class SameVariableOnlyAnalysis {

		private Tallies stats = new Tallies();

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
				node.traverse(this);
				return null;
			}

			Iterable<Call> calls() {
				return calls;
			}
		}

		public void analyse(Function function, Model model) throws Exception {

			SignatureBuilder chainAnalyser = new SignatureBuilder();

			for (BasicBlock sub : function.getCfg().getBlocks()) {
				for (Call call : new CallFinder(sub).calls()) {
					if (!isMethodCallOnName(call, function))
						continue;

					Collection<Call> dependentCalls = chainAnalyser.signature(
							MethodCallHelper.extractMethodCallTarget(call),
							sub, function, model);

					if (dependentCalls != null) {
						int count = countUniqueMethodNames(dependentCalls);
						// if (count > 1)
						// printChain(call, dependentCalls);

						stats.addTally(new Integer(count));
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
			return !(typer.typeOf(variable, function) instanceof uk.ac.ic.doc.gander.flowinference.types.TImportable);
		}

		private int countUniqueMethodNames(Iterable<Call> calls) {
			Set<String> methods = new HashSet<String>();
			for (Call call : calls) {
				methods.add(MethodCallHelper.extractMethodCallName(call).id);
			}
			return methods.size();
		}

	}

	private SameVariableOnlyAnalysis matching = new SameVariableOnlyAnalysis();
	private Model model;

	public DominationLength(Hierarchy hierarchy) throws Exception {
		this.model = new Model(hierarchy);
		this.walk(hierarchy);
	}

	public Tallies getResult() {
		return matching.stats;
	}

	@Override
	protected void visitModule(uk.ac.ic.doc.gander.hierarchy.Module value)
			throws Exception {
		Module module = model.loadModule(value.getFullyQualifiedName());
		for (Function function : module.getFunctions().values())
			analyseFunction(function);

		for (Class klass : module.getClasses().values())
			analyseClass(klass);
	}

	@Override
	protected void visitPackage(Package value) throws Exception {
		uk.ac.ic.doc.gander.model.Package pkg = model.loadPackage(value
				.getFullyQualifiedName());
		for (Function function : pkg.getFunctions().values())
			analyseFunction(function);

		for (Class klass : pkg.getClasses().values())
			analyseClass(klass);
	}

	private void analyseClass(Class klass) throws Exception {
		for (Function method : klass.getFunctions().values())
			analyseFunction(method);
	}

	private void analyseFunction(Function function) throws Exception {
		// System.err.println("Processing " + function.getFullName());
		matching.analyse(function, model);
	}
}
