package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.Expr;

public class ExprScope extends ScopeWithParent {

	private Expr node;

	public ExprScope(Expr node, Statement previousStatement,
			Statement.Exit trajectory, boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() throws Exception {
		// Expr nodes are just wrappers for any expression node. These
		// nodes need processing in their own right.

		return (Statement) this.node.value.accept(this);
	}
}
