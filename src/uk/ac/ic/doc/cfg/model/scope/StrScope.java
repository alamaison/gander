package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.Str;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class StrScope extends ScopeWithParent {

	private Str node;
	
	public StrScope(Str node, BasicBlock root, Scope parent) {
		super(parent, root);
		this.node = node;
	}

	@Override
	protected ScopeExits doProcess() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
