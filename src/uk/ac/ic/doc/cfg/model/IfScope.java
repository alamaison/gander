package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.ast.If;

public class IfScope extends ScopeWithParent {

	private If node;

	public IfScope(If node, Scope parent) throws Exception {
		super(parent);
		this.node = node;
	}

	@Override
	protected void process() throws Exception {

		BasicBlock testBlock = getCurrentBlock();
		
		node.test.accept(this);
		
		if (node.body != null) {
			BlockScope scope = new BlockScope(node.body, this);
			delegateScope(scope);

			// When no else branch, control falls through directly from the test
			// block
			if (node.orelse == null) {
				assert getCurrentBlock() != null;
				parent.fallthrough(getCurrentBlock());
			}
			
			cascadeBreakoutUpwards();
			cascadeFallthruUpwards();
		}

		if (node.orelse != null) {
			delegateScope(new BlockScope(node.orelse.body, this));
			
			cascadeBreakoutUpwards();
			cascadeFallthruUpwards();
		}
		
		parent.setCurrentBlock(testBlock);
	}

	@Override
	protected void cascadeBreakoutUpwards() {
		for (BasicBlock b : breakoutQueue) {
			if (b == null) {
				// break appears as first statement in block
				// link from our test block instead of body (which doesn't
				// exist)
				parent.breakout(getCurrentBlock());
			} else {
				parent.breakout(b);
			}
		}
		breakoutQueue.clear();
	}
}
