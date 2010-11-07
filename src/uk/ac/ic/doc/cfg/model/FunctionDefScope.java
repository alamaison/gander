package uk.ac.ic.doc.cfg.model;

import java.util.LinkedList;
import java.util.Queue;

import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.If;

public class FunctionDefScope extends Scope {
	
	private BasicBlock start = new BasicBlock();
	private BasicBlock end;

	Queue<BasicBlock> fallthroughQueue = new LinkedList<BasicBlock>();
	
	public FunctionDefScope(FunctionDef node) throws Exception {
		super();
		BasicBlock block = new BasicBlock();
		start.link(block);
		setCurrentBlock(block);
		
		node.accept(this);
	}
	
	@Override
	public Object visitFunctionDef(FunctionDef node) throws Exception {
		Object ret = super.visitFunctionDef(node);
		
		if (getCurrentBlock().isEmpty()) {
			end = getCurrentBlock();
		} else {
			end = new BasicBlock();
			linkAfterCurrent(end);
		}
		
		return ret;
	}

	@Override
	public Object visitIf(If node) throws Exception {
		IfScope scope = new IfScope(node, this);

		// Blocks may (will?) be waiting for fall-through after processing a branch
		// or a loop.  If so, we must start a new basic block and link the
		// fall-through blocks to it.
		assert fallthroughQueue.size() > 0;
		if (fallthroughQueue.size() > 0) {
			BasicBlock nextBlock = new BasicBlock();
			for (BasicBlock b : fallthroughQueue) {
				b.link(nextBlock);
			}
			fallthroughQueue.clear();
			setCurrentBlock(nextBlock);
		}
		return null;
	}

	protected void linkAfterCurrent(BasicBlock successor) {
		getCurrentBlock().link(successor);
	}
	
	protected void fallthrough(BasicBlock predecessor) {
		fallthroughQueue.add(predecessor);
	}

	public BasicBlock getStart() {
		return start;
	}
	
	public BasicBlock getEnd() {
		return end;
	}
}
