package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.VisitorBase;

public abstract class Scope extends VisitorBase {

	private BasicBlock block = null;

	@Override
	public void traverse(SimpleNode node) throws Exception {
		node.traverse(this);
	}

	protected abstract void begin();
	protected abstract void process() throws Exception;
	protected abstract void end();

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

	protected abstract void fallthrough(BasicBlock block);

	protected abstract void breakout(BasicBlock block);

	protected void linkAfterCurrent(BasicBlock successor) {
		getCurrentBlock().link(successor);
		setCurrentBlock(successor);
	}
}