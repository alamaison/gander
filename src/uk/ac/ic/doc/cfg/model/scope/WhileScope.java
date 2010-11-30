package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.While;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class WhileScope extends ScopeWithParent {

	private While node;

	public WhileScope(While node, CodeScope parent) throws Exception {
		super(parent);
		this.node = node;
	}

	@Override
	protected void doProcess() throws Exception {

		BasicBlock testBlock = newBlock();
		parent.linkAfterCurrent(testBlock);
		parent.fallthrough(testBlock);

		setCurrentBlock(testBlock);

		node.test.accept(this);

		if (node.body != null) {
			BodyScope scope = new BodyScope(node.body, this);
			scope.process();
		}

		for (BasicBlock b : fallthroughQueue)
			b.link(testBlock);
		fallthroughQueue.clear();

		// TODO Handle Python while loops that have 'else' clauses!
		// if (node.orelse != null){
		// node.orelse.accept(this);
		// }

		// breaks in the while loop fall through to whatever is after the
		// loop rather than passing through the test first
		for (BasicBlock b : breakoutQueue) {
			parent.fallthrough(b);
		}
		breakoutQueue.clear();

		parent.tail(null);
	}
}
