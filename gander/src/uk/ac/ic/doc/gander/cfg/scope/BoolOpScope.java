package uk.ac.ic.doc.gander.cfg.scope;

import org.python.pydev.parser.jython.ast.BoolOp;

import uk.ac.ic.doc.gander.cfg.scope.Statement.Exit;

public class BoolOpScope extends ScopeWithParent {

	private BoolOp node;

	public BoolOpScope(BoolOp node, Statement previousStatement,
			Exit trajectory, boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() {
		return delegate(node.values);
	}

}
