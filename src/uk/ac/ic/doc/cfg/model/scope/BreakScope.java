package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.Break;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class BreakScope extends ScopeWithParent {

	public BreakScope(Break node, BasicBlock root, Scope parent) {
		super(parent, root);
	}

	@Override
	protected ScopeExits doProcess() throws Exception {
		ScopeExits exits = new ScopeExits();
		exits.breakout(getCurrentBlock());
		exits.setRoot(getCurrentBlock());
		return exits;
	}

}
