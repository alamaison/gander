package uk.ac.ic.doc.gander.cfg.model.scope;

import uk.ac.ic.doc.gander.cfg.model.BasicBlock;

abstract class ScopeWithParent extends Scope {

	private Scope parent;

	protected ScopeWithParent(Scope parent, Statement previousStatement,
			Statement.Exit trajectory, boolean startInNewBlock) {
		super(previousStatement, trajectory, startInNewBlock);
		this.parent = parent;
	}

	@Override
	protected Statement process() throws Exception {
		assert !trajectory().isEmpty(); // Don't process unreachable code

		Statement exits = doProcess();

		assert exits.exitSize() > 0;
		assert !exits.inlinks().isEmpty();
		
		return exits;
	}

	protected abstract Statement doProcess() throws Exception;

	@Override
	protected BasicBlock newBlock() {
		return parent.newBlock();
	}
}
