package uk.ac.ic.doc.gander.cfg.scope;

import uk.ac.ic.doc.gander.cfg.BasicBlock;

class EmptyScope extends ScopeWithParent {

	private BasicBlock block;

	protected EmptyScope(Scope parent, BasicBlock block) {
		super(parent, null, null, true);
		this.block = block;
	}

	@Override
	protected final Statement process() {
		return doProcess();
	}

	@Override
	protected Statement doProcess() {
		Statement exits = new Statement();
		exits.fallthrough(block);
		exits.inlink(block);
		return exits;
	}

}
