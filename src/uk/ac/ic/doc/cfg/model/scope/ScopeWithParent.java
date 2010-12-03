package uk.ac.ic.doc.cfg.model.scope;

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

		assert exits.exitSize() > 0;
		return exits;
	}

	protected abstract ScopeExits doProcess() throws Exception;

	@Override
	protected BasicBlock newBlock() {
		return parent.newBlock();
	}
}
