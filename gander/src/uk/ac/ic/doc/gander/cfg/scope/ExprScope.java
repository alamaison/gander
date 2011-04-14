package uk.ac.ic.doc.gander.cfg.scope;

import org.python.pydev.parser.jython.ast.Expr;

class ExprScope extends ScopeWithParent {

	private Expr node;

	protected ExprScope(Expr node, Statement previousStatement,
			Statement.Exit trajectory, boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() {
		// Expr nodes are just wrappers for any expression node. These
		// nodes need processing in their own right.

		return delegate(node.value);
	}
}
