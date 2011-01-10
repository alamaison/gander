package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.Break;

class BreakScope extends ScopeWithParent {

	protected BreakScope(Break node, Statement previousStatement,
			Statement.Exit trajectory, boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
	}

	@Override
	protected Statement doProcess() throws Exception {
		Statement statement = new Statement();
		statement.breakouts().inherit(trajectory());
		return statement;
	}

}
