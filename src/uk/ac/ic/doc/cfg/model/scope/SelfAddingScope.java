package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.SimpleNode;

public class SelfAddingScope<T extends SimpleNode> extends ScopeWithParent {

	private T node;

	public SelfAddingScope(T node, Statement previousStatement,
			Statement.Exit trajectory, boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() throws Exception {
		addToCurrentBlock(node);

		Statement exits = new Statement();
		// We really mean to inherits _all_ exits from the previousStatement
		// but that statement is either:
		// - an existing one which has a single fallthrough which we added to
		// - a new one we just created and added our statement to a new
		// basic block in the fallthrough queue
		// In either case, all other exits _must_ be empty so there's no point
		// in inheriting them
		assert previousStatement().exitSize() == 1;
		exits.inheritInlinksFrom(previousStatement());
		exits.inheritFallthroughsFrom(previousStatement());

		return exits;
	}
}
