package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.ast.Break;

public class BreakScope extends ScopeWithParent {

	public BreakScope(Break node, Scope parent) {
		super(parent);
	}

	@Override
	protected void process() throws Exception {
		parent.breakout(getCurrentBlock());
	}

}
