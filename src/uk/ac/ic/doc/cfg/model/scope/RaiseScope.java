package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.Raise;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class RaiseScope extends ScopeWithParent {

	private Raise node;

	public RaiseScope(Raise node, BasicBlock root, Scope parent) {
		super(parent, root);
		this.node = node;
	}

	@Override
	protected ScopeExits doProcess() throws Exception {
		if (node.type != null)
			addToCurrentBlock(node.type);
		if (node.inst != null)
			addToCurrentBlock(node.inst);
		if (node.tback!= null)
			addToCurrentBlock(node.tback);
		
		ScopeExits exits = new ScopeExits();
		exits.raise(getCurrentBlock());
		exits.setRoot(getCurrentBlock());
		
		return exits;
	}

}
