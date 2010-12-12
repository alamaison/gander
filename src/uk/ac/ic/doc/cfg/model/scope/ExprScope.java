package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.Expr;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class ExprScope extends ScopeWithParent {

	private Expr node;

	public ExprScope(Expr node, BasicBlock root, Scope parent) {
		super(parent, root);
		this.node = node;
	}

	@Override
	protected ScopeExits doProcess() throws Exception {
		// Expr nodes are just wrappers for any expression node.  These
		// nodes need processing in their own right.
		
		return (ScopeExits) this.node.value.accept(this);
	}
}
