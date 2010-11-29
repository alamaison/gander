package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.SimpleNode;

public abstract class ScopeWithParent extends CodeScope {

	protected Scope parent;

	protected void addToCurrentBlock(SimpleNode node) {
		if (getCurrentBlock() == null) {
			BasicBlock nextBlock = newBlock();
			parent.linkAfterCurrent(nextBlock);
			setCurrentBlock(nextBlock);
		}

		assert getCurrentBlock().getSuccessors().size() == 0;
		getCurrentBlock().addStatement(node);
	}

	@Override
	protected void linkAfterCurrent(BasicBlock successor) {
		BasicBlock current = getCurrentBlock();
		if (current == null) {
			setCurrentBlock(successor);
			parent.linkAfterCurrent(successor);
		} else {
			current.link(successor);
		}
	}

	public ScopeWithParent(Scope parent) {
		this.parent = parent;
	}

	protected final void cascadeFallthruUpwards() {
		for (BasicBlock b : fallthroughQueue)
			parent.fallthrough(b);
		fallthroughQueue.clear();
	}

	protected final void cascadeBreakoutUpwards() {
		for (BasicBlock b : breakoutQueue) {
			if (b == null) {
				// break appears as first statement in block.
				// link from our current block (which should be an if/loop test
				// block) instead of body (which doesn't exist)
				parent.breakout(getCurrentBlock());
			} else {
				parent.breakout(b);
			}
		}
		breakoutQueue.clear();
	}

	protected void begin() {
		// This has to be here because Java generics don't allow delayed
		// construction (e.g. "new T") so delegateScope can't call the
		// constructor *after* changing the parent's current block.
		setCurrentBlock(parent.getCurrentBlock());
	}

	@Override
	protected final void process() throws Exception {
		begin();
		doProcess();
		end();
	}

	protected abstract void doProcess() throws Exception;

	protected void end() {
		// Any remaining fallthrough blocks should be cascaded up to the
		// parent who will tie off the loose ends.
		cascadeFallthruUpwards();
		cascadeBreakoutUpwards();
	}

	@Override
	protected BasicBlock newBlock() {
		return parent.newBlock();
	}
}
