package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.ast.Expr;

public class ExprScope extends ScopeWithParent {

	private Expr node;

	public ExprScope(Expr node, Scope parent) {
		super(parent);
		this.node = node;
	}

	@Override
	protected void process() throws Exception {
		addToCurrentBlock(node.value);
	}
}
