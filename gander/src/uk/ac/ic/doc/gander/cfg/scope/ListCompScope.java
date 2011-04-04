package uk.ac.ic.doc.gander.cfg.scope;

import org.python.pydev.parser.jython.ast.ListComp;

import uk.ac.ic.doc.gander.cfg.scope.Statement.Exit;

public class ListCompScope extends ScopeWithParent {

	private ListComp node;

	public ListCompScope(ListComp node, Statement previousStatement,
			Exit trajectory, boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() throws Exception {
		return delegate(node.elt);
		// XXX: How do we get at the generators?! The AST doesn't seem to
		// provide them!
	}

}
