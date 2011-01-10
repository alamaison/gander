package uk.ac.ic.doc.cfg.model.scope;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public abstract class ScopeWithParent extends Scope {

	protected Scope parent;

	public ScopeWithParent(Scope parent, Statement previousStatement,
			Statement.Exit trajectory, boolean forceNewBlock) {
		super(previousStatement, trajectory, forceNewBlock);
		this.parent = parent;
	}

	@Override
	protected final Statement process() throws Exception {
		Statement exits = doProcess();

		// EmptyScope triggers //assert exits.exitSize() > 0;
		return exits;
	}

	protected abstract Statement doProcess() throws Exception;

	@Override
	protected BasicBlock newBlock() {
		return parent.newBlock();
	}
}
