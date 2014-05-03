package uk.ac.ic.doc.gander.cfg.scope;

import org.python.pydev.parser.jython.ast.StrJoin;

import uk.ac.ic.doc.gander.cfg.scope.Statement.Exit;

public class StrJoinScope extends ScopeWithParent {

	private StrJoin node;

	public StrJoinScope(StrJoin node, Statement previousStatement,
			Exit trajectory, boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() {
		return delegate(node.strs);
	}

}
