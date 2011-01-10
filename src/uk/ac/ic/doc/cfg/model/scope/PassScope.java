package uk.ac.ic.doc.cfg.model.scope;

class PassScope extends ScopeWithParent {

	protected PassScope(Statement previousStatement, Statement.Exit trajectory,
			boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
	}

	@Override
	protected Statement doProcess() throws Exception {
		Statement exits = new Statement();
		exits.fallthroughs().inherit(trajectory());
		return exits;
	}

}
