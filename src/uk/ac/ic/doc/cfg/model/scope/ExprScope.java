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
		addToCurrentBlock(node.value);
		ScopeExits exits = new ScopeExits();
		exits.setRoot(getCurrentBlock());
		exits.fallthrough(getCurrentBlock());
		return exits;
	}
}
