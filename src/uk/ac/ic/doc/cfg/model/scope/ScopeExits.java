package uk.ac.ic.doc.cfg.model.scope;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class ScopeExits {

	private Set<BasicBlock> fallthroughQueue = new HashSet<BasicBlock>();
	private Set<BasicBlock> breakoutQueue = new HashSet<BasicBlock>();
	private BasicBlock root;

	public void fallthrough(BasicBlock block) {
		fallthroughQueue.add(block);
	}

	public void breakout(BasicBlock block) {
		breakoutQueue.add(block);
	}

	public Set<BasicBlock> getFallthroughQueue() {
		return fallthroughQueue;
	}

	public void linkFallThroughsTo(BasicBlock successor) {
		assert !isEmpty();
		for (BasicBlock fallBlock : fallthroughQueue)
			fallBlock.link(successor);
	}

	public void linkFallThroughsTo(ScopeExits successor) {
		linkFallThroughsTo(successor.getRoot());
	}

	public void inheritFallthroughsFrom(ScopeExits otherExits) {
		fallthroughQueue.addAll(otherExits.getFallthroughQueue());
	}

	public void inheritBreakoutsFrom(ScopeExits otherExits) {
		breakoutQueue.addAll(otherExits.getBreakoutQueue());
	}

	public void inheritExitsFrom(ScopeExits otherExits) {
		inheritFallthroughsFrom(otherExits);
		inheritBreakoutsFrom(otherExits);
	}

	public void convertBreakoutsToFallthroughs(ScopeExits successor) {
		fallthroughQueue.addAll(successor.breakoutQueue);
	}

	public int exitSize() {
		return fallthroughQueue.size() + breakoutQueue.size();
	}

	public boolean isEndOfBlock() {
		if (exitSize() > 1)
			return true;

		assert fallthroughQueue.size() < 2;
		for (BasicBlock b : fallthroughQueue) {
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

	public void setFallthroughQueue(Set<BasicBlock> fallthroughQueue) {
		this.fallthroughQueue = fallthroughQueue;
	}

	public Set<BasicBlock> getBreakoutQueue() {
		return breakoutQueue;
	}

	public void setBreakoutQueue(Set<BasicBlock> breakoutQueue) {
		this.breakoutQueue = breakoutQueue;
	}

	public BasicBlock getRoot() {
		return root;
	}

	public void setRoot(BasicBlock root) {
		this.root = root;
	}

	@Override
	public String toString() {
		return "ROOT:\n" + stringise(root) + "\n\nFALLTHROUGH:\n"
				+ stringise(fallthroughQueue) + "\n\nBREAKOUT:\n"
				+ stringise(breakoutQueue);
	}

	private static String stringise(Object obj) {
		return (obj == null) ? "NULL" : obj.toString();
	}
}