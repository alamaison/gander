package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.Continue;

public class ContinueScope extends ScopeWithParent {

	public ContinueScope(Continue node, Statement previousStatement,
			Statement.Exit trajectory, boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
	}

	@Override
	protected Statement doProcess() throws Exception {
		Statement statement = new Statement();
		statement.continues().inherit(previousStatement().fallthroughs());
		return statement;
	}

}
