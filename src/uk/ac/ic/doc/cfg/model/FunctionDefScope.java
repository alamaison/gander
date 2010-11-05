package uk.ac.ic.doc.cfg.model;

import java.util.ArrayList;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.If;

public class FunctionDefScope extends Scope {
	
	private BasicBlock start = new BasicBlock();
	private BasicBlock end = new BasicBlock();

	ArrayList<BasicBlock> waitingFallthroughBlocks = new ArrayList<BasicBlock>();
	
	public FunctionDefScope(FunctionDef node) throws Exception {
		super();
		node.accept(this);
		start.link(getCurrentBlock());
	}
	
	@Override
	public Object visitFunctionDef(FunctionDef node) throws Exception {
		Object ret = super.visitFunctionDef(node);
		finish();
		return ret;
	}

	@Override
	public Object visitIf(If node) throws Exception {
		addToCurrentBlock(node);
		IfScope scope = new IfScope(node, this);

		return null;
	}
	
	@Override
	public void traverse(SimpleNode node) throws Exception {
		// Blocks may be waiting for fall-through after processing a branch
		// or a loop.  If so, we must start a new basic block and link the
		// fall-through blocks to it.
		if (waitingFallthroughBlocks.size() > 0) {
			BasicBlock nextBlock = new BasicBlock();
			linkAfterCurrent(nextBlock);
			setCurrentBlock(nextBlock);
			for (BasicBlock b : waitingFallthroughBlocks) {
				b.link(getCurrentBlock());
				waitingFallthroughBlocks.remove(b);
			}
		}
		
		super.traverse(node);
	}

	protected void linkAfterCurrent(BasicBlock successor) {
		getCurrentBlock().link(successor);
	}
	
	protected void linkFallthrough(BasicBlock predecessor) {
		waitingFallthroughBlocks.add(predecessor);
	}
	
	@Override
	protected void finish() {
		// Any remaining fall-through blocks can only go to function END
		for (BasicBlock b : waitingFallthroughBlocks) {
			b.link(end);
		}
		
		getCurrentBlock().link(end);
	}

	public BasicBlock getStart() {
		return start;
	}
	
	public BasicBlock getEnd() {
		return end;
	}
}
