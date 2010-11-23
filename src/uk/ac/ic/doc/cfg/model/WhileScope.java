package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.ast.While;

public class WhileScope extends ScopeWithParent {

	private While node;

	public WhileScope(While node, CodeScope parent) throws Exception {
		super(parent);
		this.node = node;
	}

	@Override
	protected void process() throws Exception {
		boolean isParentEmpty = parent.getCurrentBlock().isEmpty();

		BasicBlock testBlock;
		if (isParentEmpty)
			testBlock = parent.getCurrentBlock();
		else
			testBlock = newBlock();
		parent.fallthrough(testBlock);
		
		setCurrentBlock(testBlock);

		if (node.test != null) {
			node.test.accept(this);
		}

		BasicBlock bodyBlock = newBlock();
		testBlock.link(bodyBlock);
		setCurrentBlock(bodyBlock);

		if (node.body != null) {
			for (int i = 0; i < node.body.length; i++) {
				if (node.body[i] != null) {
					node.body[i].accept(this);
				}
			}
		}

		// TODO Handle Python while loops that have 'else' clauses!
		// if (node.orelse != null){
		// node.orelse.accept(this);
		// }

		// linkAfterCurrent(testBlock);
		// parent.fallthrough(getCurrentBlock());

		if (fallthroughQueue.isEmpty()) {
			getCurrentBlock().link(testBlock);
		} else {
			for (BasicBlock b : fallthroughQueue)
				b.link(testBlock);
		}

		if (!isParentEmpty)
			parent.linkAfterCurrent(testBlock);
	}
}
