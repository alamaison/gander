/**
 * 
 */
package uk.ac.ic.doc.gander.analysis.signatures;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.analysis.FunctionResolver;
import uk.ac.ic.doc.gander.analysis.dominance.Postdomination;
import uk.ac.ic.doc.gander.analysis.signatures.PassedVariableFinder.PassedVar;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Namespace;

/**
 * Given a variable name, return the part of its signature that is derived from
 * looking at any other calls its passed to.
 * 
 * An important feature of this class is that it maintains a stack of functions
 * as it digs through possible call chains which is needed to prevent infinitely
 * recursion when analysing a recursive function.
 */
final class PartialSignatureFromPassingVariable {

	private Stack<Namespace> enclosingScopes = new Stack<Namespace>();

	/**
	 * Return the signature produced by passing the given variable as a
	 * parameter to other calls.
	 * 
	 * Only calls occurring in the given basic blocks, {@code blocksToSearch},
	 * are analysed to generate the signature. The idea is that these are some
	 * set of control-dependent blocks with respect to a location of interest in
	 * the program.
	 * 
	 * Any uses of the variable in the called functions, as well as any uses
	 * from passing the variable to further functions, are included in the
	 * signature.
	 * 
	 * @param variable
	 *            Name of the variable whose uses are being analysed. This is
	 *            the name of the variable within the enclosing function,
	 *            {@code enclosingFunction}, as the name may be something
	 *            completely different when mapped to the parameter in the
	 *            function being called.
	 * @param blocksToSearch
	 *            Control-dependent blocks within the enclosing function,
	 *            {@code enclosingFunction}, that are searched for calls to
	 *            which {@code variable} is passed as a parameter.
	 * @param enclosingScope
	 *            Function being searched for uses of {@code variable} as a
	 *            function parameter.
	 * @param model
	 *            Runtime model of the system needed to resolve function names
	 *            to function implementations.
	 * @return Partial signature of 'uses' as a set of method calls.
	 */
	Set<Call> buildSignature(String variable,
			Iterable<BasicBlock> blocksToSearch, Namespace enclosingScope,
			TypeResolver resolver) {
		enclosingScopes.push(enclosingScope);

		Set<Call> calls = new HashSet<Call>();

		Set<PassedVar> passes = new PassedVariableFinder(variable,
				blocksToSearch).passes();
		for (PassedVar pass : passes) {
			Call call = pass.getCall();
			Function function = resolveFunction(resolver, enclosingScope, call);
			if (function != null) {
				// Must stop if we have already processed a call to the
				// resolved function
				// FIXME: should only really stop if the parameter was
				// passed at the same position too.
				if (!enclosingScopes.contains(function)) {
					calls.addAll(calculateSignatureForPassedVariable(pass,
							function, resolver));
				}
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
	private Set<Call> buildSignatureForFunctionParameter(Name param,
			Function function, TypeResolver resolver) {
		Set<BasicBlock> blocks = inevitableBlocks(function);

		Set<Call> calls = new PartialSignatureFromUsingVariable()
				.buildSignature(param, blocks, function.getCfg());

		calls.addAll(buildSignature(param.id, blocks, function, resolver));
		return calls;
	}

	/**
	 * Return signature implied by passing variable to call.
	 */
	private Set<Call> calculateSignatureForPassedVariable(PassedVar pass,
			Function function, TypeResolver resolver) {

		Set<Call> calls = new HashSet<Call>();

		for (Name param : resolveParameterNames(pass, function.getAst())) {
			calls.addAll(buildSignatureForFunctionParameter(param, function,
					resolver));
		}

		return calls;
	}

	private static Function resolveFunction(TypeResolver resolver,
			Namespace enclosingScope, Call call) {
		Function function = new FunctionResolver(call, enclosingScope, resolver)
				.getFunction();

		if (function == null)
			System.err.println("Warning: unable to resolve function: "
					+ call.func);

		return function;
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
			// called. This can happen if the called function has a stararg
			// parameter. E.g. def func(*args) being called as func(1,2).
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

	/**
	 * Return the blocks which will always execute when a function executes.
	 * 
	 * This is all blocks that post-dominate the start node. Nothing can
	 * dominate the start node so dominator analysis is not needed to calculate
	 * control-dependent blocks.
	 */
	private static Set<BasicBlock> inevitableBlocks(Function function) {
		Cfg graph = function.getCfg();

		Set<BasicBlock> controlDependentBlocks = new HashSet<BasicBlock>();

		controlDependentBlocks.add(graph.getStart());

		Postdomination postdom = new Postdomination(graph);
		controlDependentBlocks.addAll(postdom.dominators(graph.getStart()));

		return controlDependentBlocks;
	}
}