package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.ast.Assign;

public class AssignScope extends ScopeWithParent {

	private Assign node;

	public AssignScope(Assign node, Scope parent) {
		super(parent);
		this.node = node;
	}

	@Override
	protected void doProcess() throws Exception {
		addToCurrentBlock(node);
	}
}
	