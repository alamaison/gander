package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.SimpleNode;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class BodyScope extends ScopeWithParent {

	private SimpleNode[] nodes;

	public BodyScope(SimpleNode[] nodes, BasicBlock root, Scope parent) {
		super(parent, root);
		this.nodes = nodes;
	}

	@Override
	protected ScopeExits doProcess() throws Exception {

		BasicBlock bodyRoot = null;
		ScopeExits body = new ScopeExits();
		ScopeExits previousStatement = new ScopeExits();
		ScopeExits lastStatement = new ScopeExits();

		for (int i = 0; i < nodes.length; i++) {

			// A statement the generated multiple internal or external links
			// forces us to close the current basic block. We signal this to the
			// next processor by setting the block to null. The processor will
			// create a new block if necessary.
			if (getCurrentBlock() != null && lastStatement.isEndOfBlock())
				setCurrentBlock(null);

			previousStatement = lastStatement;
			lastStatement = (ScopeExits) nodes[i].accept(this);

			// The first statement that actually contributes a basic
			// block is the root block for this body
			if (bodyRoot == null)
				bodyRoot = lastStatement.getRoot();

			// The statement processor may have added to the current basic block
			// or it may have started a new one. We can detect this by comparing
			// the root it returns with the current block. If they don't match
			// then the processor started a new basic block. In this case we
			// must link the current block to the new root.
			if (getCurrentBlock() != lastStatement.getRoot()) {
				previousStatement.linkFallThroughsTo(lastStatement);
				setCurrentBlock(lastStatement.getRoot());
			}

			// Union all statements' other exits as we don't process them at
			// this level
			body.inheritAllButFallthroughsFrom(lastStatement);

			// It's possible we didn't have a current block. If so, we pretend
			// we did for the next loop
			if (getCurrentBlock() == null)
				setCurrentBlock(lastStatement.getRoot());
		}

		// Only push up last statement's fallthroughs as the others are
		// dealt with by the body processing loop here
		body.inheritFallthroughsFrom(lastStatement);

		body.setRoot(bodyRoot);

		assert body.exitSize() > 0;
		return body;
	}
}
