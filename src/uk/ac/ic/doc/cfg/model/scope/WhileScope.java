package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.While;

public class WhileScope extends ScopeWithParent {

	private While node;

	public WhileScope(While node, Statement previousStatement,
			Statement.Exit trajectory, boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() throws Exception {

		// while

		// force new block for test
		Statement condition = delegateScopeForceNew(node.test);
		assert condition.exitSize() == 1;

		// body

		Statement body = new BodyScope(node.body, condition,
				condition.fallthroughs(), this).process();

		Statement exits = new Statement();
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

		// returns and yields are the only type of exit that is pushed to the
		// next level
		exits.inheritNonLoopExitsFrom(body);

		exits.inheritInlinksFrom(condition);
		return exits;
	}
}
