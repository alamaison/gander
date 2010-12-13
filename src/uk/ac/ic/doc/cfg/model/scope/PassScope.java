package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.Pass;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class PassScope extends ScopeWithParent {

	public PassScope(Pass node, BasicBlock root, Scope parent) {
		super(parent, root);
	}

	@Override
	protected ScopeExits doProcess() throws Exception {
		ScopeExits exits = new ScopeExits();
		exits.setRoot(getCurrentBlock());
		exits.fallthrough(getCurrentBlock());
		return exits;
	}

}