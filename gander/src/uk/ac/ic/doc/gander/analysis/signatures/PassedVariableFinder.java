package uk.ac.ic.doc.gander.analysis.signatures;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.exprType;
import org.python.pydev.parser.jython.ast.keywordType;

import uk.ac.ic.doc.gander.analysis.BasicBlockTraverser;
import uk.ac.ic.doc.gander.cfg.BasicBlock;

/**
 * Find where the given variable is passed to calls as a parameter.
 */
public class PassedVariableFinder extends BasicBlockTraverser {

	/**
	 * Details of a variable's passing to a call.
	 * 
	 * A variable may be passed more than once to a single call and can appear
	 * by position and/or by keyword.
	 */
	public class PassedVar {

		private Set<Integer> positions = new HashSet<Integer>();
		private Set<String> keywords = new HashSet<String>();
		private Call function;

		public PassedVar(Call call) {
			function = call;
			for (int i = 0; i < call.args.length; ++i)
				if (isNameMatch(call.args[i]))
					positions.add(i);
			for (keywordType kw : call.keywords)
				if (isNameMatch(kw.value))
					keywords.add(((NameTok) kw.arg).id);
		}
		
		public Call getCall() {
			return function;
		}

		public Set<Integer> getPositions() {
			return positions;
		}

		public Set<String> getKeywords() {
			return keywords;
		}
	}

	private Set<PassedVar> calls = new HashSet<PassedVar>();
	private String variable;

	public PassedVariableFinder(String variable, Iterable<BasicBlock> blocks)
			throws Exception {
		this.variable = variable;
		for (BasicBlock block : blocks) {
			for (SimpleNode node : block) {
				node.accept(this);
			}
		}
	}

	@Override
	public Object visitCall(Call node) throws Exception {
		if (isVariablePassed(node))
			calls.add(new PassedVar(node));
		
		// Some arguments may themselves be calls so we need to dig deeper
		node.traverse(this);
		return null;
	}

	private boolean isNameMatch(exprType candidate) {
		return candidate instanceof Name
				&& ((Name) candidate).id.equals(variable);
	}

	private boolean isVariablePassed(Call call) {
		for (exprType expr : call.args)
			if (isNameMatch(expr))
				return true;

		for (keywordType kw : call.keywords)
			if (isNameMatch(kw.value))
				return true;

		// TODO: Deal with kwargs (what is it?!)
		// TODO: Deal with starargs

		return false;
	}

	public Set<PassedVar> passes() {
		return calls;
	}
}