package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.If;
import org.python.pydev.parser.jython.ast.stmtType;

public class IfScope extends ScopeWithParent {

	private If node;

	public IfScope(If node, Statement previousStatement,
			Statement.Exit trajectory, boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() throws Exception {

		Statement exits = new Statement();

		// if

		Statement condition = (Statement) node.test.accept(this);

		// then

		exits.inheritExitsFrom(processBranch(node.body, condition));

		// else

		// When there is only a then branch, control falls through directly
		// from the test block otherwise it falls from both branches by *not*
		// the test block
		if (node.orelse == null)
			exits.inheritExitsFrom(condition);
		else
			exits.inheritExitsFrom(processBranch(node.orelse.body, condition));

		exits.inheritInlinksFrom(condition);
		return exits;
	}

	private Statement processBranch(stmtType[] branch, Statement condition)
			throws Exception {

		assert condition.exitSize() == 1;
		Statement body = new BodyScope(branch, condition,
				condition.fallthroughs(), this).process();

		return body;
	}
}
