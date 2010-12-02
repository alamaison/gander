package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.Assign;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class AssignScope extends ScopeWithParent {

	private Assign node;

	public AssignScope(Assign node, BasicBlock root, Scope parent) {
		super(parent, root);
		this.node = node;
	}

	@Override
	protected ScopeExits doProcess() throws Exception {
		addToCurrentBlock(node);
		ScopeExits exits = new ScopeExits();
		exits.setRoot(getCurrentBlock());
		exits.fallthrough(getCurrentBlock());
		return exits;
	}
}
	