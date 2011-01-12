package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.List;

import uk.ac.ic.doc.cfg.model.scope.Statement.Exit;

public class ListScope extends ScopeWithParent {

	private List node;

	public ListScope(List node, Statement previousStatement, Exit trajectory,
			boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() throws Exception {
		return delegate(node.elts);
	}

}
