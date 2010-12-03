package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.Continue;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class ContinueScope extends ScopeWithParent {

	public ContinueScope(Continue node, BasicBlock root, Scope parent) {
		super(parent, root);
	}

	@Override
	protected ScopeExits doProcess() throws Exception {
		ScopeExits exits = new ScopeExits();
		exits.continu(getCurrentBlock());
		exits.setRoot(getCurrentBlock());
		return exits;
	}

}
