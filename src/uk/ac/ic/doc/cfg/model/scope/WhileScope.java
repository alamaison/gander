package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.While;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class WhileScope extends ScopeWithParent {

	private While node;

	public WhileScope(While node, BasicBlock root, CodeScope parent)
			throws Exception {
		super(parent, root);
		this.node = node;
	}

	@Override
	protected ScopeExits doProcess() throws Exception {

		ScopeExits exits = new ScopeExits();

		// while

		BasicBlock testBlock = newBlock();
		setCurrentBlock(testBlock);
		ScopeExits condition = (ScopeExits) node.test.accept(this);

		// body

		BodyScope scope = new BodyScope(node.body, null, this);
		ScopeExits body = scope.process();

		// link the test block to the then body and the fallthrough
		assert condition.getFallthroughQueue().size() == 1;
		for (BasicBlock b : condition.getFallthroughQueue()) {
			b.link(body.getRoot());
			exits.fallthrough(b);
		}

		// link the body back to the condition
		for (BasicBlock b : body.getFallthroughQueue())
			b.link(condition.getRoot());

		// TODO Handle Python while loops that have 'else' clauses!
		// if (node.orelse != null){
		// node.orelse.accept(this);
		// }

		// breaks in the while loop fall through to whatever is after the
		// loop rather than passing through the test first
		for (BasicBlock b : body.getBreakoutQueue()) {
			exits.fallthrough(b);
		}

		exits.setRoot(condition.getRoot());
		return exits;
	}
}
