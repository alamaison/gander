package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.Yield;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class YieldScope extends ScopeWithParent {

	private Yield node;

	public YieldScope(Yield node, BasicBlock root, Scope parent) {
		super(parent, root);
		this.node = node;
	}

	@Override
	protected ScopeExits doProcess() throws Exception {
		addToCurrentBlock(node.value);
		
		ScopeExits exits = new ScopeExits();
		exits.setRoot(getCurrentBlock());
		
		// This isn't really how yield works but, for our purposes, it
		// may suffice.
		// TODO: Handle yield correctly
		exits.fallthrough(getCurrentBlock());
		return exits;
	}

}
