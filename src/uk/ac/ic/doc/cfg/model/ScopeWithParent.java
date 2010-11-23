package uk.ac.ic.doc.cfg.model;

public abstract class ScopeWithParent extends CodeScope {

	protected Scope parent;

	public ScopeWithParent(Scope parent) {
		this.parent = parent;
		setCurrentBlock(parent.getCurrentBlock());
	}
	
	protected void cascadeFallthruUpwards() {
		for (BasicBlock fallBlock : fallthroughQueue)
			parent.fallthrough(fallBlock);
	}

	@Override
	protected void end() {
		// Any remaining fallthrough blocks should be cascaded up to the
		// parent who will tie off the loose ends.
		cascadeFallthruUpwards();
	}
	
	@Override
	protected BasicBlock newBlock() {
		return parent.newBlock();
	}
}
