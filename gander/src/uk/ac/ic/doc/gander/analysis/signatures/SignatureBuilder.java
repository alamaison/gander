package uk.ac.ic.doc.gander.analysis.signatures;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.analysis.BasicBlockTraverser;
import uk.ac.ic.doc.gander.analysis.FunctionResolver;
import uk.ac.ic.doc.gander.analysis.dominance.Domination;
import uk.ac.ic.doc.gander.analysis.dominance.Postdomination;
import uk.ac.ic.doc.gander.analysis.signatures.PassedVariableFinder.PassedVar;
import uk.ac.ic.doc.gander.analysis.ssa.SSAVariableSubscripts;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;

public class SignatureBuilder {

	/**
	 * Return the blocks which are control-dependent on the given block.
	 */
	private static Set<BasicBlock> controlDependentBlocks(BasicBlock block,
			Cfg graph) {
		Set<BasicBlock> controlDependentBlocks = new HashSet<BasicBlock>();

		controlDependentBlocks.add(block);

		Domination dom = new Domination(graph);
		controlDependentBlocks.addAll(dom.dominators(block));

		Postdomination postdom = new Postdomination(graph);
		controlDependentBlocks.addAll(postdom.dominators(block));

		return controlDependentBlocks;
	}

	/**
	 * Finds calls that target a given SSA subscripted variable in a single
	 * basic block.
	 */
	private static class IntraBlockVariableMatcher {

		private Set<Call> calls = new HashSet<Call>();
		private Name target;
		private int subscript;
		private SSAVariableSubscripts renamer;

		public IntraBlockVariableMatcher(BasicBlock containingBlock,
				Name target, int subscript, SSAVariableSubscripts renamer) {
			this.target = target;
			this.subscript = subscript;
			this.renamer = renamer;
			for (SimpleNode node : containingBlock) {
				try {
					node.accept(new VariableMatcherVisitor());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		private class VariableMatcherVisitor extends BasicBlockTraverser {

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

				// Calls may contain other calls as parameters so continue
				// digging into AST
				node.traverse(this);

				return null;
			}
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

	/**
	 * Build a signature for the given variable by looking at the given blocks
	 * and recursing into any calls they make.
	 * 
	 * Signatures are in the form of a set of all calls that must be executed in
	 * every case on the object held in the variable. This is the set of calls
	 * that are control-dependent on the given name and operate not only on the
	 * same name but on the same SSA renaming of the name. This ensures that
	 * calls which may happen after re-assigning to a variable aren't included.
	 */
	public Set<Call> signature(Name variable, BasicBlock containingBlock,
			Function enclosingFunction, Model model) {
		return buildSignature(variable, containingBlock, enclosingFunction,
				model);
	}

	/**
	 * Build a signature for the given variable by looking at the given blocks
	 * and recursing into any calls they make.
	 */
	private static Set<Call> buildSignature(Name variable,
			BasicBlock controlBlock, Function function, Model model) {

		// FIXME: This will loop infinitely with recursive calls

		Set<BasicBlock> blocks = controlDependentBlocks(controlBlock, function
				.getCfg());

		Set<Call> calls = getPartialSignatureFromVariableUseInFunction(
				variable, blocks, function);

		calls.addAll(getPartialSignatureFromPassingVariableToCalls(variable.id,
				blocks, function, model));
		return calls;
	}

	/**
	 * Given a particular use of a variable, return the part of its signature
	 * that is derived from uses occurring solely within the same function.
	 */
	private static Set<Call> getPartialSignatureFromVariableUseInFunction(
			Name variableAtLocation, Set<BasicBlock> blocksToSearch,
			Function function) {
		Set<Call> calls = new HashSet<Call>();

		SSAVariableSubscripts ssa = new SSAVariableSubscripts(function.getCfg());
		int permittedSubscript = ssa.subscript(variableAtLocation);

		for (BasicBlock block : blocksToSearch) {
			IntraBlockVariableMatcher matcher = new IntraBlockVariableMatcher(
					block, variableAtLocation, permittedSubscript, ssa);
			calls.addAll(matcher.matchingCalls());
		}

		return calls;
	}

	/**
	 * Given a variable name, return the part of its signature that is derived
	 * from looking at any other calls its passed to.
	 */
	private static Set<Call> getPartialSignatureFromPassingVariableToCalls(
			String variable, Iterable<BasicBlock> blocksToSearch,
			Function enclosingFunction, Model model) {

		Set<Call> calls = new HashSet<Call>();

		Set<PassedVar> passes = new PassedVariableFinder(variable,
				blocksToSearch).passes();
		for (PassedVar pass : passes) {
			Call call = pass.getCall();
			Function function = resolveFunction(model, enclosingFunction, call);
			if (function != null) {
				calls.addAll(getSignatureForPassedVariable(pass, function,
						model));
			}

			return calls;
		}

		return calls;
	}

	private static Function resolveFunction(Model model,
			Function enclosingFunction, Call call) {
		Function function = new FunctionResolver(call, enclosingFunction, model)
				.getFunction();

		if (function == null)
			System.err.println("Warning: unable to resolve function: "
					+ call.func);

		return function;
	}

	/**
	 * Return signature implied by passing variable to call.
	 */
	private static Set<Call> getSignatureForPassedVariable(PassedVar pass,
			Function function, Model model) {

		Set<Call> calls = new HashSet<Call>();

		for (Name param : resolveParameterNames(pass, function.getFunctionDef())) {
			calls.addAll(getSignatureForParameter(param, function, model));
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
	private static Set<Call> getSignatureForParameter(Name param,
			Function function, Model model) {
		Cfg graph = function.getCfg();
		return buildSignature(param, graph.getStart(), function, model);
	}

	/**
	 * Map passing specification to the parameter names they correspond to in
	 * the function being called.
	 */
	private static Set<Name> resolveParameterNames(PassedVar pass,
			FunctionDef function) {
		Set<Name> names = new HashSet<Name>();

		for (Integer pos : pass.getPositions()) {
			// The passing specification may indicate that more parameters
			// are passed than there are parameters in the function being
			// called.  This can happen if the called function has a stararg
			// parameter.  E.g. def func(*args) being called as func(1,2).
			if (pos < function.args.args.length)
				names.add((Name) function.args.args[pos]);
		}

		for (String keyword : pass.getKeywords()) {
			Name name = findParamNameNode(keyword, function);
			if (name == null)
				System.err.println("PROGRAM ERROR: Function called with "
						+ "incorrect keyword");
			else
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
