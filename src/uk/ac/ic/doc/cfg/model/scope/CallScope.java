package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class CallScope extends ScopeWithParent {

	private Call node;

	// TODO Make CallScope that handles exceptions killing the basic block
	public CallScope(Call node, BasicBlock root, Scope parent) {
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
