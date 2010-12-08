package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.For;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class ForScope extends ScopeWithParent {

	private For node;

	public ForScope(For node, BasicBlock root, Scope parent) {
		super(parent, root);
		this.node = node;
	}

	@Override
	protected ScopeExits doProcess() throws Exception {
		ScopeExits exits = new ScopeExits();

		// for

		setCurrentBlock(null); // force new block for iterable
		ScopeExits iterable = (ScopeExits) node.iter.accept(this);

		// body

		BodyScope scope = new BodyScope(node.body, null, this);
		ScopeExits body = scope.process();

		// link the iterable to the body and the fallthrough
		assert iterable.exitSize() == 1;
		iterable.linkFallThroughsTo(body);
		exits.inheritFallthroughsFrom(iterable);

		// link the body back to the iterable
		body.linkFallThroughsTo(iterable);

		// continues in the while loop link back to the iterable
		body.linkContinuesTo(iterable);

		// TODO Handle Python for loops that have 'else' clauses!
		// if (node.orelse != null){
		// node.orelse.accept(this);
		// }

		// breaks in the while loop fall through to whatever is after the
		// loop rather than passing through the test first
		exits.convertBreakoutsToFallthroughs(body);

		// returns are the only type of exit that is pushed to the next level
		exits.inheritReturnsFrom(body);

		exits.setRoot(iterable.getRoot());
		return exits;
	}

}
