package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.Raise;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class RaiseScope extends ScopeWithParent {

	public RaiseScope(Raise node, BasicBlock root, Scope parent) {
		super(parent, root);
	}

	@Override
	protected ScopeExits doProcess() throws Exception {
		ScopeExits exits = new ScopeExits();
		exits.raise(getCurrentBlock());
		exits.setRoot(getCurrentBlock());
		
		return exits;
	}

}
