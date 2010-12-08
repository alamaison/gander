package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.Return;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class ReturnScope extends ScopeWithParent {

	private Return node;

	public ReturnScope(Return node, BasicBlock root, Scope parent) {
		super(parent, root);
		this.node = node;
	}
	
	@Override
	protected ScopeExits doProcess() throws Exception {
		if (node.value != null) {
			addToCurrentBlock(node.value);
		}
		
		ScopeExits exits = new ScopeExits();
		exits.returnFrom(getCurrentBlock());
		exits.setRoot(getCurrentBlock());
		
		return exits;
	}

}
