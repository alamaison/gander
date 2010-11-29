package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.ast.Call;

public class CallScope extends ScopeWithParent {

	private Call node;

	// TODO Make CallScope that handles exceptions killing the basic block
	public CallScope(Call node, Scope parent) {
		super(parent);
		this.node = node;
	}

	@Override
	protected void doProcess() throws Exception {
		addToCurrentBlock(node);
	}

}
