package uk.ac.ic.doc.gander.cfg.scope;

import org.python.pydev.parser.jython.ast.If;
import org.python.pydev.parser.jython.ast.stmtType;

class IfScope extends ScopeWithParent {

	private If node;

	protected IfScope(If node, Statement previousStatement,
			Statement.Exit trajectory, boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() {

		Statement exits = new Statement();

		// if

		Statement condition = delegate(node.test);

		// then

		exits.inheritExitsFrom(processBranch(node.body, condition));

		// else

		// When there is only a then branch, control falls through directly
		// from the test block otherwise it falls from both branches by *not*
		// the test block
		if (node.orelse == null)
			exits.inheritExitsFrom(condition);
		else {
			exits.inheritExitsFrom(processBranch(node.orelse.body, condition));
			exits.inheritAllButFallthroughsFrom(condition);
		}

		exits.inheritInlinksFrom(condition);
		return exits;
	}

	private Statement processBranch(stmtType[] branch, Statement condition) {

		return buildGraphForceNewBlock(branch, condition, condition
				.fallthroughs());
	}
}
