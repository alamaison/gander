package uk.ac.ic.doc.gander.cfg.scope;

import org.python.pydev.parser.jython.ast.Break;

class BreakScope extends ScopeWithParent {

	protected BreakScope(Break node, Statement previousStatement,
			Statement.Exit trajectory, boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
	}

	@Override
	protected Statement doProcess() throws Exception {
		Statement statement = new Statement();
		statement.inlinks().inherit(trajectory());
		statement.breakouts().inherit(trajectory());
		return statement;
	}

}
