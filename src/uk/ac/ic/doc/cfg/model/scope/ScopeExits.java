package uk.ac.ic.doc.cfg.model.scope;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class ScopeExits {

	private class Exit {
		private Set<BasicBlock> blocks = new HashSet<BasicBlock>();

		public void add(BasicBlock block) {
			blocks.add(block);
		}

		public void inherit(Exit other) {
			blocks.addAll(other.blocks);
		}

		public void linkTo(ScopeExits successor) {
			linkTo(successor.getRoot());
		}

		public void linkTo(BasicBlock successor) {
			for (BasicBlock b : blocks)
				b.link(successor);
		}

		public boolean isEmpty() {
			return blocks.isEmpty();
		}

		public int size() {
			return blocks.size();
		}

		/**
		 * Replace null placeholders with actual root block.
		 * 
		 * Some statements such as break or continue don't contribute any
		 * expressions to a basic block. If they are the first statement in a
		 * body, there isn't a current block yet so we add null to the
		 * corresponding exit list. When the body processor returns, these nulls
		 * must be replaced by the actual block they will cause control to flow
		 * from.
		 */
		public void replaceNullExits(BasicBlock root) {
			for (BasicBlock b : blocks) {
				if (b == null) {
					blocks.remove(b);
					blocks.add(root);
				}
			}
		}

		@Override
		public String toString() {
			return blocks.toString();
		}
	}

	private Exit fallthroughQueue = new Exit();
	private Exit breakoutQueue = new Exit();
	private Exit continueQueue = new Exit();
	private Exit returnQueue = new Exit();
	private Exit raiseQueue = new Exit();

	private BasicBlock root;

	public Exit fallthroughs() {
		return fallthroughQueue;
	}

	public Exit breakouts() {
		return breakoutQueue;
	}

	public Exit continues() {
		return continueQueue;
	}

	public Exit returns() {
		return returnQueue;
	}

	public Exit raises() {
		return raiseQueue;
	}

	public void fallthrough(BasicBlock block) {
		fallthroughs().add(block);
	}

	public void breakout(BasicBlock block) {
		breakouts().add(block);
	}

	public void continu(BasicBlock block) {
		continues().add(block);
	}

	public void returnFrom(BasicBlock block) {
		returns().add(block);
	}

	public void raise(BasicBlock block) {
		raises().add(block);
	}

	public void linkFallThroughsTo(BasicBlock successor) {
		fallthroughs().linkTo(successor);
	}

	public void linkFallThroughsTo(ScopeExits successor) {
		fallthroughs().linkTo(successor);
	}

	public void linkContinuesTo(ScopeExits successor) {
		continues().linkTo(successor);
	}

	public void linkReturnsTo(BasicBlock successor) {
		returns().linkTo(successor);
	}

	public void linkReturnsTo(ScopeExits successor) {
		returns().linkTo(successor);
	}

	public void linkRaisesTo(ScopeExits successor) {
		raises().linkTo(successor);
	}

	public void linkRaisesTo(BasicBlock successor) {
		raises().linkTo(successor);
	}

	public void linkBreaksTo(ScopeExits successor) {
		breakouts().linkTo(successor);
	}

	public boolean canRaise() {
		return !raises().isEmpty();
	}

	public boolean canReturn() {
		return !returns().isEmpty();
	}

	public boolean canFallThrough() {
		return !fallthroughs().isEmpty();
	}

	public boolean canBreak() {
		return !breakouts().isEmpty();
	}

	public void inheritFallthroughsFrom(ScopeExits otherExits) {
		fallthroughs().inherit(otherExits.fallthroughs());
	}

	public void inheritNonLoopExitsFrom(ScopeExits otherExits) {
		returns().inherit(otherExits.returns());
		inheritRaisesFrom(otherExits);
	}

	public void inheritRaisesFrom(ScopeExits otherExits) {
		raises().inherit(otherExits.raises());
	}

	public void inheritAllButFallthroughsFrom(ScopeExits otherExits) {

		breakouts().inherit(otherExits.breakouts());
		continues().inherit(otherExits.continues());
		returns().inherit(otherExits.returns());
		inheritRaisesFrom(otherExits);
	}

	public void inheritExitsFrom(ScopeExits otherExits) {
		inheritAllButFallthroughsFrom(otherExits);
		inheritFallthroughsFrom(otherExits);
	}

	public void convertBreakoutsToFallthroughs(ScopeExits successor) {
		fallthroughs().inherit(successor.breakouts());
	}

	public void convertFallthroughsToRaises(ScopeExits successor) {
		raises().inherit(successor.fallthroughs());
	}

	public void convertFallthroughsToReturns(ScopeExits successor) {
		returns().inherit(successor.fallthroughs());
	}

	public void convertFallthroughsToBreaks(ScopeExits successor) {
		breakouts().inherit(successor.fallthroughs());
	}

	public int exitSize() {
		return fallthroughs().size() + breakouts().size() + continues().size()
				+ returns().size() + raises().size();
	}

	public boolean isEndOfBlock() {
		if (exitSize() > 1)
			return true;

		// If the one fallthrough block already has a successor
		assert fallthroughs().size() < 2;
		for (BasicBlock b : fallthroughs().blocks) {
			if (!b.isEnd())
				return true;
		}

		return false;
	}

	/**
	 * State of statement's basic blocks.
	 * 
	 * @return true if statement had no expressions and produced no basic
	 *         blocks.
	 */
	public boolean isEmpty() {
		return getRoot() == null;
	}

	public BasicBlock getRoot() {
		return root;
	}

	public void setRoot(BasicBlock root) {
		this.root = root;
		replaceNullExits();
	}

	/**
	 * Replace null placeholders with actual root block.
	 * 
	 * Some statements such as break or continue don't contribute any
	 * expressions to a basic block. If they are the first statement in a body,
	 * there isn't a current block yet so we add null to the corresponding exit
	 * list. When the body processor returns, these nulls must be replaced by
	 * the actual block they will cause control to flow from.
	 */
	private void replaceNullExits() {
		fallthroughs().replaceNullExits(getRoot());
		breakouts().replaceNullExits(getRoot());
		continues().replaceNullExits(getRoot());
		returns().replaceNullExits(getRoot());
		raises().replaceNullExits(getRoot());
	}

	@Override
	public String toString() {
		return "ROOT:\n" + stringise(root) + "\n\nFALLTHROUGH:\n"
				+ stringise(fallthroughs()) + "\n\nBREAKOUT:\n"
				+ stringise(breakouts()) + "\n\nCONTINUE:\n"
				+ stringise(continues()) + "\n\nRETURN:\n"
				+ stringise(returns()) + "\n\nRAISE:\n" + stringise(raises());
	}

	private static String stringise(Object obj) {
		return (obj == null) ? "NULL" : obj.toString();
	}
}