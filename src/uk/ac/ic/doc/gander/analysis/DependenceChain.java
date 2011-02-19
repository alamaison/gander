package uk.ac.ic.doc.gander.analysis;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.analysis.PassedVariableFinder.Passing;
import uk.ac.ic.doc.gander.analysis.dominance.Domination;
import uk.ac.ic.doc.gander.analysis.dominance.Postdomination;
import uk.ac.ic.doc.gander.cfg.model.BasicBlock;
import uk.ac.ic.doc.gander.cfg.model.Cfg;
import uk.ac.ic.doc.gander.cfg.model.Function;
import uk.ac.ic.doc.gander.cfg.model.Module;
import uk.ac.ic.doc.gander.flowinference.types.TypeResolutionVisitor;

public class DependenceChain {

	private Set<BasicBlock> controlDependentBlocks(BasicBlock containingBlock,
			Cfg graph, VariableRenaming renamer) {
		Set<BasicBlock> controlDependentBlocks = new HashSet<BasicBlock>();

		controlDependentBlocks.add(containingBlock);

		Domination dom = new Domination(renamer.getDomInfo());
		controlDependentBlocks.addAll(dom.dominators(containingBlock));

		Postdomination postdom = new Postdomination(graph);
		controlDependentBlocks.addAll(postdom.dominators(containingBlock));

		return controlDependentBlocks;
	}

	public static class ChainFinder {

		private Set<Call> chain = new HashSet<Call>();

		public ChainFinder(Name target, Set<BasicBlock> blocks, Module module,
				Cfg graph, VariableRenaming renamer) throws Exception {

			TypeResolutionVisitor typer = new TypeResolutionVisitor(module
					.getAst());
			if (typer.typeOf(target.id) instanceof uk.ac.ic.doc.gander.flowinference.types.Module)
				return;

			int permittedSubscript = renamer.subscript(target);

			for (BasicBlock dependentBlock : blocks) {
				addMatches(target, dependentBlock, permittedSubscript, renamer);
			}
		}

		private void addMatches(Name target, BasicBlock containingBlock,
				int permittedSubscript, VariableRenaming renamer)
				throws Exception {
			IntraBlockVariableMatcher matcher = new IntraBlockVariableMatcher(
					containingBlock, target, permittedSubscript, renamer);
			chain.addAll(matcher.matchingCalls());
		}

		public Set<Call> dependentCalls() {
			return chain;
		}
	}

	/**
	 * Finds calls that target a given SSA subscripted variable in a single
	 * basic block.
	 */
	private static class IntraBlockVariableMatcher extends BasicBlockTraverser {

		private Set<Call> calls = new HashSet<Call>();
		private Name target;
		private int subscript;
		private VariableRenaming renamer;

		public IntraBlockVariableMatcher(BasicBlock containingBlock,
				Name target, int subscript, VariableRenaming renamer)
				throws Exception {
			this.target = target;
			this.subscript = subscript;
			this.renamer = renamer;
			for (SimpleNode node : containingBlock) {
				node.accept(this);
			}
		}

		@Override
		public Object visitCall(Call node) throws Exception {
			if (node.func instanceof Attribute) {
				Attribute fieldAccess = (Attribute) node.func;
				if (fieldAccess.value instanceof Name) {
					Name candidate = (Name) fieldAccess.value;

					if (isMatch(candidate))
						calls.add(node);
				}
			}
			return null;
		}

		private boolean isMatch(Name candidate) {
			return isNameMatch(candidate) && isSubscriptMatch(candidate);
		}

		private boolean isNameMatch(Name candidate) {
			return target.id.equals(candidate.id);
		}

		private boolean isSubscriptMatch(Name candidate) {
			return subscript == renamer.subscript(candidate);
		}

		Set<Call> matchingCalls() {
			return calls;
		}
	}

	private Cfg graph;
	private Module module;
	private VariableRenaming renamer;

	public DependenceChain(Module module, Cfg graph) throws Exception {
		this.module = module;
		this.graph = graph;
		renamer = new VariableRenaming(graph);
	}

	private static Name extractMethodCallTarget(Call call) {
		Attribute fieldAccess = (Attribute) call.func;
		return (Name) fieldAccess.value;
	}

	/**
	 * Given a call, return all calls that target the same variable that are
	 * control-dependent on the original call.
	 */
	public Set<Call> dependentCalls(Call call, BasicBlock containingBlock)
			throws Exception {

		Name target = extractMethodCallTarget(call);

		Set<BasicBlock> controlDependentBlocks = controlDependentBlocks(
				containingBlock, graph, renamer);

		Set<Call> calls = new ChainFinder(target, controlDependentBlocks,
				module, graph, renamer).dependentCalls();

		Set<Passing> passes = new PassedVariableFinder(target.id,
				controlDependentBlocks).passes();
		for (Passing pass : passes) {
			Call subCall = pass.getCall();

			if (subCall.func instanceof Name) {
				// TODO: deal with situation where function is not a simple
				// variable name. It might be qualified with a module name, for
				// instance.
				Function function = module.getFunctions().get(
						((Name) subCall.func).id);
				if (function == null) {
					System.err
							.println("Warning: unable to find called function: "
									+ ((Name) subCall.func).id);
				} else {
					FunctionDef functiondef = function.getFunctionDef();
					calls.addAll(subGraphParamControlDependentBlocks(pass,
							functiondef));
				}
			} else {
				System.err
						.println("Warning: variable passed to a function that we can't yet resolve: "
								+ subCall.func);
			}
		}

		return calls;
	}

	/**
	 * With respect to a given parameter in a function, return the set of
	 * methods calls on the parameter that are always executed if the function
	 * is called.
	 * 
	 * This is the set of calls that postdominate the entry node of the
	 * function.
	 */
	private Collection<Call> subGraphParamControlDependentBlocks(Passing pass,
			FunctionDef function) throws Exception {

		Set<Call> calls = new HashSet<Call>();

		Cfg graph = new Cfg(function);
		Postdomination postdom = new Postdomination(graph);
		for (Name param : resolveParameterNames(pass, function)) {
			ChainFinder finder = new ChainFinder(param,
					new HashSet<BasicBlock>(postdom
							.dominators(graph.getStart())), module, graph,
					new VariableRenaming(graph));
			calls.addAll(finder.dependentCalls());
		}

		return calls;
	}

	/**
	 * Map passing specification to the parameter names they correspond to in
	 * the function being called.
	 */
	private static Set<Name> resolveParameterNames(Passing pass,
			FunctionDef function) {
		Set<Name> names = new HashSet<Name>();

		for (Integer pos : pass.getPositions()) {
			names.add((Name) function.args.args[pos]);
		}

		for (String keyword : pass.getKeywords()) {
			Name name = findParamNameNode(keyword, function);
			if (name == null)
				throw new Error("Function called with incorrect keyword");
			names.add(name);
		}

		return names;
	}

	private static Name findParamNameNode(String name, FunctionDef function) {
		for (exprType expr : function.args.args) {
			Name candidate = ((Name) expr);
			if (candidate.id.equals(name))
				return candidate;
		}
		return null;
	}
}
