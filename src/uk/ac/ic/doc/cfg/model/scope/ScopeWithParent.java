package uk.ac.ic.doc.cfg.model.scope;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public abstract class ScopeWithParent extends CodeScope {

	protected Scope parent;

	protected void addToCurrentBlock(SimpleNode node) {
		if (getCurrentBlock() == null) {
			BasicBlock nextBlock = newBlock();
			setCurrentBlock(nextBlock);
		}

		assert getCurrentBlock().getSuccessors().size() == 0;
		getCurrentBlock().addStatement(node);
	}

	protected void linkAfterCurrent(BasicBlock successor) {
		BasicBlock current = getCurrentBlock();
		if (current == null) {
			setCurrentBlock(successor);
		} else {
			current.link(successor);
		}
	}

	public ScopeWithParent(Scope parent, BasicBlock root) {
		super();
		setCurrentBlock(root);
		this.parent = parent;
	}

	@Override
	protected final ScopeExits process() throws Exception {
		ScopeExits exits = doProcess();
		Set<BasicBlock> filteredBreakouts = new HashSet<BasicBlock>();
		
		for (BasicBlock b : exits.getBreakoutQueue()) {
			if (b == null) {
				// break appears as first statement in block.
				// link from statement's root (which should be an if/loop test
				// block) instead of body (which doesn't exist)
				filteredBreakouts.add(exits.getRoot());
			} else {
				filteredBreakouts.add(b);
			}
		}
		
		exits.setBreakoutQueue(filteredBreakouts);
		
		return exits;
	}

	protected abstract ScopeExits doProcess() throws Exception;

	@Override
	protected BasicBlock newBlock() {
		return parent.newBlock();
	}
}
