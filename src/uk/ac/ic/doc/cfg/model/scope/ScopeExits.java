package uk.ac.ic.doc.cfg.model.scope;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class ScopeExits {
	@Override
	public String toString() {
		return "ROOT:\n" + stringise(root) + "\n\nFALLTHROUGH:\n"
				+ stringise(fallthroughQueue) + "\n\nBREAKOUT:\n"
				+ stringise(breakoutQueue);
	}

	private static String stringise(Object obj) {
		return (obj == null) ? "NULL" : obj.toString();
	}

	private Set<BasicBlock> fallthroughQueue = new HashSet<BasicBlock>();
	private Set<BasicBlock> breakoutQueue = new HashSet<BasicBlock>();
	private BasicBlock tail;
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

	public void setFallthroughQueue(Set<BasicBlock> fallthroughQueue) {
		this.fallthroughQueue = fallthroughQueue;
	}

	public Set<BasicBlock> getBreakoutQueue() {
		return breakoutQueue;
	}

	public void setBreakoutQueue(Set<BasicBlock> breakoutQueue) {
		this.breakoutQueue = breakoutQueue;
	}

	public BasicBlock getTail() {
		return tail;
	}

	public BasicBlock getRoot() {
		return root;
	}

	public void tail(BasicBlock tail) {
		this.tail = tail;
	}

	public void union(ScopeExits otherExits) {
		fallthroughQueue.addAll(otherExits.getFallthroughQueue());
		breakoutQueue.addAll(otherExits.getBreakoutQueue());
	}

	public void setRoot(BasicBlock root) {
		this.root = root;
	}
}