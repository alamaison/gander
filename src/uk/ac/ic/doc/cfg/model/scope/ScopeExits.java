package uk.ac.ic.doc.cfg.model.scope;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class ScopeExits {

	private Set<BasicBlock> fallthroughQueue = new HashSet<BasicBlock>();
	private Set<BasicBlock> breakoutQueue = new HashSet<BasicBlock>();
	private Set<BasicBlock> continueQueue = new HashSet<BasicBlock>();
	private Set<BasicBlock> returnQueue = new HashSet<BasicBlock>();
	private Set<BasicBlock> raiseQueue = new HashSet<BasicBlock>();
	private BasicBlock root;

	public void fallthrough(BasicBlock block) {
		fallthroughQueue.add(block);
	}

	public void breakout(BasicBlock block) {
		breakoutQueue.add(block);
	}

	public void continu(BasicBlock block) {
		continueQueue.add(block);
	}

	public void returnFrom(BasicBlock block) {
		returnQueue.add(block);
	}

	public void raise(BasicBlock block) {
		raiseQueue.add(block);
	}

	public void linkFallThroughsTo(BasicBlock successor) {
		for (BasicBlock fallBlock : fallthroughQueue)
			fallBlock.link(successor);
	}

	public void linkFallThroughsTo(ScopeExits successor) {
		linkFallThroughsTo(successor.getRoot());
	}

	public void linkContinuesTo(ScopeExits successor) {
		for (BasicBlock continueBlock : continueQueue)
			continueBlock.link(successor.getRoot());
	}

	public void linkReturnsTo(BasicBlock successor) {
		for (BasicBlock returnBlock : returnQueue)
			returnBlock.link(successor);
	}
	
	public void linkReturnsTo(ScopeExits successor) {
		linkReturnsTo(successor.getRoot());
	}

	public void linkRaisesTo(ScopeExits successor) {
		linkRaisesTo(successor.getRoot());
	}

	public void linkRaisesTo(BasicBlock successor) {
		for (BasicBlock block : raiseQueue)
			block.link(successor);
	}

	public void linkBreaksTo(ScopeExits successor) {
		for (BasicBlock block : breakoutQueue)
			block.link(successor.getRoot());
	}

	public boolean canRaise() {
		return !raiseQueue.isEmpty();
	}

	public boolean canReturn() {
		return !returnQueue.isEmpty();
	}

	public boolean canFallThrough() {
		return !fallthroughQueue.isEmpty();
	}

	public boolean canBreak() {
		return !breakoutQueue.isEmpty();
	}

	public void inheritFallthroughsFrom(ScopeExits otherExits) {
		fallthroughQueue.addAll(otherExits.fallthroughQueue);
	}

	public void inheritNonLoopExitsFrom(ScopeExits otherExits) {
		returnQueue.addAll(otherExits.returnQueue);
		inheritRaisesFrom(otherExits);
	}

	public void inheritRaisesFrom(ScopeExits otherExits) {
		raiseQueue.addAll(otherExits.raiseQueue);
	}

	public void inheritAllButFallthroughsFrom(ScopeExits otherExits) {

		breakoutQueue.addAll(otherExits.breakoutQueue);
		continueQueue.addAll(otherExits.continueQueue);
		returnQueue.addAll(otherExits.returnQueue);
		inheritRaisesFrom(otherExits);
	}

	public void inheritExitsFrom(ScopeExits otherExits) {
		inheritAllButFallthroughsFrom(otherExits);
		inheritFallthroughsFrom(otherExits);
	}

	public void convertBreakoutsToFallthroughs(ScopeExits successor) {
		fallthroughQueue.addAll(successor.breakoutQueue);
	}

	public void convertFallthroughsToRaises(ScopeExits successor) {
		raiseQueue.addAll(successor.fallthroughQueue);
	}

	public void convertFallthroughsToReturns(ScopeExits successor) {
		returnQueue.addAll(successor.fallthroughQueue);
	}

	public void convertFallthroughsToBreaks(ScopeExits successor) {
		breakoutQueue.addAll(successor.fallthroughQueue);
	}

	public int exitSize() {
		return fallthroughQueue.size() + breakoutQueue.size()
				+ continueQueue.size() + returnQueue.size() + raiseQueue.size();
	}

	public boolean isEndOfBlock() {
		if (exitSize() > 1)
			return true;

		// If the one fallthrough block already has a successor
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
		replaceNullExits(fallthroughQueue, getRoot());
		replaceNullExits(breakoutQueue, getRoot());
		replaceNullExits(continueQueue, getRoot());
		replaceNullExits(returnQueue, getRoot());
		replaceNullExits(raiseQueue, getRoot());
	}

	private static void replaceNullExits(Set<BasicBlock> exits, BasicBlock root) {
		for (BasicBlock b : exits) {
			if (b == null) {
				exits.remove(b);
				exits.add(root);
			}
		}
	}

	@Override
	public String toString() {
		return "ROOT:\n" + stringise(root) + "\n\nFALLTHROUGH:\n"
				+ stringise(fallthroughQueue) + "\n\nBREAKOUT:\n"
				+ stringise(breakoutQueue) + "\n\nCONTINUE:\n"
				+ stringise(continueQueue) + "\n\nRETURN:\n"
				+ stringise(returnQueue) + "\n\nRAISE:\n"
				+ stringise(raiseQueue);
	}

	private static String stringise(Object obj) {
		return (obj == null) ? "NULL" : obj.toString();
	}
}