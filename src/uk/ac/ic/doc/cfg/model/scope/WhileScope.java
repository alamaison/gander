package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.While;

class WhileScope extends ScopeWithParent {

	private While node;

	protected WhileScope(While node, Statement previousStatement,
			Statement.Exit trajectory, boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() throws Exception {
		Statement exits = new Statement();

		// while

		// force new block for test
		Statement condition = delegateButForceNewBlock(node.test);

		exits.inheritInlinksFrom(condition);
		exits.inheritExitsFrom(condition);

		// body

		Statement body = buildGraphForceNewBlock(node.body, condition,
				condition.fallthroughs());

		// link the body back to the condition
		body.linkFallThroughsTo(condition);

		// continues in the while loop link back to the condition
		body.linkContinuesTo(condition);

		// TODO Handle Python while loops that have 'else' clauses!
		if (node.orelse != null) {
			// node.orelse.accept(this);
			System.err.println("WARNING: unhandled while/else");
		}

		// breaks in the while loop fall through to whatever is after the
		// loop rather than passing through the test first
		exits.convertBreakoutsToFallthroughs(body);

		// returns and yields are the only type of exit that is pushed to the
		// next level
		exits.inheritNonLoopExitsFrom(body);

		return exits;
	}
}
