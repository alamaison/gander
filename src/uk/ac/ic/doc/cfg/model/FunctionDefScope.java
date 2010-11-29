package uk.ac.ic.doc.cfg.model;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.FunctionDef;

public class FunctionDefScope extends CodeScope {

	private BasicBlock start;
	private BasicBlock end;
	private FunctionDef node;
	private Set<BasicBlock> blocks = new HashSet<BasicBlock>();

	public FunctionDefScope(FunctionDef node) throws Exception {
		super();
		this.node = node;
		start = newBlock();
		BasicBlock block = newBlock();
		start.link(block);
		setCurrentBlock(block);
	}

	protected BasicBlock newBlock() {
		BasicBlock b = new BasicBlock();
		blocks.add(b);
		return b;
	}

	public Set<BasicBlock> getBlocks() {
		return blocks;
	}

	public void process() throws Exception {
		node.traverse(this);

		if (getCurrentBlock().isEmpty()) {
			end = getCurrentBlock();
		} else {
			end = newBlock();
		}

		for (BasicBlock b : fallthroughQueue) {
			b.link(end);
		}
		fallthroughQueue.clear();
		
		for (BasicBlock b : blocks) {
			if (b.getSuccessors().isEmpty() && b != end) {
				b.link(end);
			}
		}
	}

	public BasicBlock getStart() {
		return start;
	}

	public BasicBlock getEnd() {
		return end;
	}
}
