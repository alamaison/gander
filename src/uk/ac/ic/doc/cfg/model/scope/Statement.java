package uk.ac.ic.doc.cfg.model.scope;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class Statement {

	private class BlockSet implements Iterable<BasicBlock> {

		protected Set<BasicBlock> blocks = new HashSet<BasicBlock>();

		public void add(BasicBlock block) {
			blocks.add(block);
		}

		public void inherit(BlockSet other) {
			blocks.addAll(other.blocks);
		}

		public boolean isEmpty() {
			return blocks.isEmpty();
		}

		public int size() {
			return blocks.size();
		}

		public Iterator<BasicBlock> iterator() {
			return blocks.iterator();
		}

		@Override
		public String toString() {
			return blocks.toString();
		}

		@Override
		public int hashCode() {
			return blocks.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof BlockSet))
				return false;

			return blocks.equals(((BlockSet) obj).blocks);
		}
	}

	public class Entry extends BlockSet {

	}

	public class Exit extends BlockSet {

		public void linkTo(Statement successor) {
			for (BasicBlock entryBlock : successor.inlinks())
				for (BasicBlock exitBlock : this)
					exitBlock.link(entryBlock);
		}
	}

	private Exit fallthroughQueue = new Exit();
	private Exit breakoutQueue = new Exit();
	private Exit continueQueue = new Exit();
	private Exit returnQueue = new Exit();
	private Exit raiseQueue = new Exit();

	private Entry inlinkQueue = new Entry();

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

	public Entry inlinks() {
		return inlinkQueue;
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

	public void inlink(BasicBlock block) {
		inlinks().add(block);
	}

	public void linkFallThroughsTo(Statement successor) {
		fallthroughs().linkTo(successor);
	}

	public void linkContinuesTo(Statement successor) {
		continues().linkTo(successor);
	}


	public void linkReturnsTo(Statement successor) {
		returns().linkTo(successor);
	}

	public void linkRaisesTo(Statement successor) {
		raises().linkTo(successor);
	}


	public void linkBreaksTo(Statement successor) {
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

	public void inheritFallthroughsFrom(Statement otherExits) {
		fallthroughs().inherit(otherExits.fallthroughs());
	}

	public void inheritNonLoopExitsFrom(Statement otherExits) {
		returns().inherit(otherExits.returns());
		inheritRaisesFrom(otherExits);
	}

	public void inheritRaisesFrom(Statement otherExits) {
		raises().inherit(otherExits.raises());
	}

	public void inheritInlinksFrom(Statement other) {
		inlinks().inherit(other.inlinks());
	}

	public void inheritAllButFallthroughsFrom(Statement otherExits) {

		breakouts().inherit(otherExits.breakouts());
		continues().inherit(otherExits.continues());
		returns().inherit(otherExits.returns());
		inheritRaisesFrom(otherExits);
	}

	public void inheritExitsFrom(Statement otherExits) {
		inheritAllButFallthroughsFrom(otherExits);
		inheritFallthroughsFrom(otherExits);
	}

	public void convertBreakoutsToFallthroughs(Statement successor) {
		fallthroughs().inherit(successor.breakouts());
	}

	public void convertFallthroughsToRaises(Statement successor) {
		raises().inherit(successor.fallthroughs());
	}

	public void convertFallthroughsToReturns(Statement successor) {
		returns().inherit(successor.fallthroughs());
	}

	public void convertFallthroughsToBreaks(Statement successor) {
		breakouts().inherit(successor.fallthroughs());
	}

	/**
	 * In many cases a statement will result in a single fallthrough block
	 * 
	 * @return Statement's unique fallthrough block.
	 */
	public BasicBlock uniqueFallthrough() {
		if (fallthroughs().size() != 1)
			throw new Error("No unique fallthough block");

		return fallthroughs().iterator().next();
	}

	public int exitSize() {
		return fallthroughs().size() + breakouts().size() + continues().size()
				+ returns().size() + raises().size();
	}

	public boolean isEndOfBlock() {
		if (exitSize() > 1)
			return true;

		// If the one fallthrough block already has a successor
		return canFallThrough() && uniqueFallthrough().isClosed();
	}

	@Override
	public String toString() {
		return "\n\nINLINK:\n" + stringise(inlinks()) + "\n\nFALLTHROUGH:\n"
				+ stringise(fallthroughs()) + "\n\nBREAKOUT:\n"
				+ stringise(breakouts()) + "\n\nCONTINUE:\n"
				+ stringise(continues()) + "\n\nRETURN:\n"
				+ stringise(returns()) + "\n\nRAISE:\n" + stringise(raises());
	}

	private static String stringise(Object obj) {
		return (obj == null) ? "null" : obj.toString();
	}
}