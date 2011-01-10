package uk.ac.ic.doc.cfg.model.scope;

import uk.ac.ic.doc.cfg.model.BasicBlock;

class EmptyScope extends ScopeWithParent {

	private BasicBlock block;

	protected EmptyScope(Scope parent, BasicBlock block) {
		super(parent, null, null, true);
		this.block = block;
	}

	@Override
	protected final Statement process() throws Exception {
		return doProcess();
	}

	@Override
	protected Statement doProcess() throws Exception {
		Statement exits = new Statement();
		exits.fallthrough(block);
		exits.inlink(block);
		return exits;
	}

}
