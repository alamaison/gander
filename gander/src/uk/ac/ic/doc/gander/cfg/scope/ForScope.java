package uk.ac.ic.doc.gander.cfg.scope;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.For;

class ForScope extends ScopeWithParent {

	private For node;

	protected ForScope(For node, Statement previousStatement,
			Statement.Exit trajectory, boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() {
		Statement exits = new Statement();

		// for

		// force new block for iterable
		Statement iterable = delegateButForceNewBlock(
				new SimpleNode[]{node.iter, node.target});

		exits.inheritInlinksFrom(iterable);
		exits.inheritExitsFrom(iterable);

		// body

		Statement body = buildGraphForceNewBlock(node.body, iterable,
				iterable.fallthroughs());

		// link the iterable to the body and the fallthrough
		iterable.linkFallThroughsTo(body);

		// link the body back to the iterable
		body.linkFallThroughsTo(iterable);

		// continues in the while loop link back to the iterable
		body.linkContinuesTo(iterable);

		// TODO Handle Python for loops that have 'else' clauses!
		if (node.orelse != null) {
			// node.orelse.accept(this);
			System.err.println("WARNING: unhandled for/else");
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
