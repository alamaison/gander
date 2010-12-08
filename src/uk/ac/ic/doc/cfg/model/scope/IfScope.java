package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.If;
import org.python.pydev.parser.jython.ast.stmtType;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class IfScope extends ScopeWithParent {

	private If node;

	public IfScope(If node, BasicBlock root, Scope parent) throws Exception {
		super(parent, root);
		this.node = node;
	}

	@Override
	protected ScopeExits doProcess() throws Exception {

		ScopeExits exits = new ScopeExits();

		// if

		ScopeExits condition = (ScopeExits) node.test.accept(this);

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

		exits.setRoot(condition.getRoot());
		return exits;
	}

	private ScopeExits processBranch(stmtType[] branch, ScopeExits condition)
			throws Exception {

		ScopeExits body = new BodyScope(branch, null, this).process();

		// link the test block to the branch body
		assert condition.exitSize() == 1;
		if (!body.isEmpty())
			condition.linkFallThroughsTo(body);

		return body;
	}
}
