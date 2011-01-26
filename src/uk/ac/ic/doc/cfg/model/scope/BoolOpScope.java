package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.BoolOp;

import uk.ac.ic.doc.cfg.model.scope.Statement.Exit;

public class BoolOpScope extends ScopeWithParent {

	private BoolOp node;

	public BoolOpScope(BoolOp node, Statement previousStatement,
			Exit trajectory, boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() throws Exception {
		return delegate(node.values);
	}

}
