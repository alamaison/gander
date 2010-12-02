package uk.ac.ic.doc.cfg.model.scope;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.FunctionDef;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class FunctionDefScope extends CodeScope {

	private BasicBlock start;
	private BasicBlock end;
	private FunctionDef node;
	private Set<BasicBlock> blocks = new HashSet<BasicBlock>();

	public FunctionDefScope(FunctionDef node) throws Exception {
		super();
		this.node = node;
	}

	public ScopeExits process() throws Exception {
		
		BodyScope scope = new BodyScope(node.body, null, this);
		
		ScopeExits state = scope.process();
		
		start = newBlock();
		if (state.getRoot() != null) {
			start.link(state.getRoot());
		}

		end = newBlock();
		for (BasicBlock b : state.getFallthroughQueue()) {
			b.link(end);
		}
		
		return new ScopeExits();
	}

	protected BasicBlock newBlock() {
		BasicBlock b = new BasicBlock();
		blocks.add(b);
		return b;
	}

	public Set<BasicBlock> getBlocks() {
		return blocks;
	}

	public BasicBlock getStart() {
		return start;
	}

	public BasicBlock getEnd() {
		return end;
	}
}
