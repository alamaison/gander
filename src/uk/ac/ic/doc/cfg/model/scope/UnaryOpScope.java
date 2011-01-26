package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.UnaryOp;

import uk.ac.ic.doc.cfg.model.scope.Statement.Exit;

public class UnaryOpScope extends ScopeWithParent {

	private UnaryOp node;

	public UnaryOpScope(UnaryOp node, Statement previousStatement,
			Exit trajectory, boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() throws Exception {
		return delegate(node.operand);
	}

}
