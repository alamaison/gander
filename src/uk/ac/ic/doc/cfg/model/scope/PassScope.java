package uk.ac.ic.doc.cfg.model.scope;

public class PassScope extends ScopeWithParent {

	public PassScope(Statement previousStatement, Statement.Exit trajectory,
			boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
	}

	@Override
	protected Statement doProcess() throws Exception {
		Statement exits = new Statement();
		exits.inheritExitsFrom(previousStatement());
		return exits;
	}

}
