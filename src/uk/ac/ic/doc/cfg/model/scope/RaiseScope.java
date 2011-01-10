package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.Raise;

class RaiseScope extends ScopeWithParent {

	private Raise node;

	protected RaiseScope(Raise node, Statement previousStatement,
			Statement.Exit trajectory, boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() throws Exception {
		Statement type = delegateScopeContinuing(node.type);

		Statement inst = delegateScope(node.inst, type, type.fallthroughs(),
				false);

		Statement tback = delegateScope(node.tback, inst, inst.fallthroughs(),
				false);

		Statement statement = new Statement();

		// take inlink from first part of exception spec that provides one
		// and attribute raise to first part's fallthough (should be
		// same as inlink
		if (type.canFallThrough()) {
			statement.inheritInlinksFrom(type);
			statement.convertFallthroughsToRaises(type);
		} else if (inst.canFallThrough()) {
			statement.inheritInlinksFrom(inst);
			statement.convertFallthroughsToRaises(inst);
		} else if (tback.canFallThrough()) {
			statement.inheritInlinksFrom(tback);
			statement.convertFallthroughsToRaises(tback);
		} else {
			// if there is a naked 'raise' statement, attribute the raising to
			// the previous statement's incoming trajectory
			statement.raises().inherit(trajectory());
		}

		statement.inheritAllButFallthroughsFrom(type);
		statement.inheritAllButFallthroughsFrom(inst);
		statement.inheritAllButFallthroughsFrom(tback);

		return statement;
	}
}
