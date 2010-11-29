package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.VisitorBase;

public abstract class Scope extends VisitorBase {

	private BasicBlock block = null;

	@Override
	public void traverse(SimpleNode node) throws Exception {
		node.traverse(this);
	}

	protected abstract void process() throws Exception;

	protected abstract BasicBlock newBlock();

	@Override
	protected Object unhandled_node(SimpleNode node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	protected BasicBlock getCurrentBlock() {
		return block;
	}

	protected void setCurrentBlock(BasicBlock block) {
		this.block = block;
	}

	/**
	 * Add block to set which fall out of subscope.
	 * 
	 * Called by child scopes of this scope.
	 * 
	 * Caller indicates that block will link to whatever (as yet undiscovered)
	 * successor block follows the statement being processed.
	 * 
	 * @param block
	 */
	protected abstract void fallthrough(BasicBlock block);

	/**
	 * Given block returns control to statement after closest loop.
	 * 
	 * @param block
	 */
	protected abstract void breakout(BasicBlock block);

	/**
	 * Block is last <em>potential</em> fallthrough in current suite.
	 * 
	 * When processing a suite of statements, the end of the suite is not yet
	 * known so a statement's processor can't yet determine if it is the
	 * fallthrough statement. The statements processor indicates to the parent
	 * suite processor that it *may* be considered as a fallthrough by calling
	 * this method. It is up to the suite processor to decide if it is a
	 * fallthrough based on whether it is the last statement in the suite. Each
	 * successive call wipes out the previously set tail as only one block can
	 * fall through in this way.
	 * 
	 * If the statement processor must indicate that its current basic block
	 * <em>cannot</em> be used as a fallthrough (for instance, the test block in
	 * an if/then/else) then it must call this method on its parent passing null
	 * as the block parameter.
	 * 
	 * @param block
	 * @see fallthrough()
	 */
	protected abstract void tail(BasicBlock block);

	protected void linkAfterCurrent(BasicBlock successor) {
		getCurrentBlock().link(successor);
		setCurrentBlock(successor);
	}
}