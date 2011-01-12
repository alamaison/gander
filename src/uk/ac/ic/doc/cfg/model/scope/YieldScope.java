package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.Yield;

class YieldScope extends ScopeWithParent {

	private Yield node;

	protected YieldScope(Yield node, Statement previousStatement,
			Statement.Exit trajectory, boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() throws Exception {
		addToCurrentBlock(node.value);

		Statement statement = new Statement();

		// This isn't really how yield works but, for our purposes, it
		// may suffice.
		// TODO: Handle yield correctly
		statement.inlinks().inherit(trajectory());
		statement.fallthroughs().inherit(trajectory());
		return statement;
	}

}
