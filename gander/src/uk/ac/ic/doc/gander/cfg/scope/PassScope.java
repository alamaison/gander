package uk.ac.ic.doc.gander.cfg.scope;

class PassScope extends ScopeWithParent {

	protected PassScope(Statement previousStatement, Statement.Exit trajectory,
			boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
	}

	@Override
	protected Statement doProcess() {
		Statement exits = new Statement();
		exits.inlinks().inherit(trajectory());
		exits.fallthroughs().inherit(trajectory());
		return exits;
	}

}
