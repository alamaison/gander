package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.SimpleNode;

public class BodyScope extends ScopeWithParent {

	private SimpleNode[] nodes;

	public BodyScope(SimpleNode[] nodes, Statement previousStatement,
			Statement.Exit trajectory, Scope parent) {
		super(parent, previousStatement, trajectory, true);
		this.nodes = nodes;
	}

	@Override
	protected Statement doProcess() throws Exception {
		Statement body = new Statement();
		Statement previousStatement = null;
		Statement lastStatement = null;

		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] == null)
				continue;

			previousStatement = lastStatement;
			lastStatement = (Statement) nodes[i].accept(this);

			// The first statement we process decides the inlinks for the
			// entire body
			if (previousStatement == null)
				body.inheritInlinksFrom(lastStatement);

			setPreviousStatement(lastStatement);
			setTrajectory(lastStatement.fallthroughs());
			setStartInNewBlock(false);

			// Fallthroughs are handles by passing them to the next statement
			// as the incoming trajectory. We union all other exits as we
			// don't handles them at this level
			body.inheritAllButFallthroughsFrom(lastStatement);
		}

		// Only push up last statement's fallthroughs as the others are
		// dealt with by the body processing loop above
		if (lastStatement != null)
			body.inheritFallthroughsFrom(lastStatement);

		// assert body.exitSize() > 0;
		return body;
	}
}
