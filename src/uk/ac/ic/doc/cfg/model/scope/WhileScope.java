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
		assert condition.exitSize() == 1;
		condition.linkFallThroughsTo(body);
		exits.inheritFallthroughsFrom(condition);

		// link the body back to the condition
		body.linkFallThroughsTo(condition);

		// continues in the while loop link back to the condition
		body.linkContinuesTo(condition);

		// TODO Handle Python while loops that have 'else' clauses!
		// if (node.orelse != null){
		// node.orelse.accept(this);
		// }

		// breaks in the while loop fall through to whatever is after the
		// loop rather than passing through the test first
		exits.convertBreakoutsToFallthroughs(body);

		// returns are the only type of exit that is pushed to the next level
		exits.inheritReturnsFrom(body);

		exits.setRoot(condition.getRoot());
		return exits;
	}
}
