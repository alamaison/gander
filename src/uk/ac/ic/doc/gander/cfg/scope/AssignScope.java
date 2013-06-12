package uk.ac.ic.doc.gander.cfg.scope;

import org.python.pydev.parser.jython.ast.Assign;

import uk.ac.ic.doc.gander.cfg.scope.Statement.Exit;

public class AssignScope extends ScopeWithParent {

	private Assign node;

	public AssignScope(Assign node, Statement previousStatement,
			Exit trajectory, boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() {
		Statement exits = new Statement();
		
		Statement rhs = delegate(node.value);
		
		exits.inheritAllButFallthroughsFrom(rhs);
		
		Statement lhsExits = buildGraph(node.targets, rhs, rhs.fallthroughs());
		exits.inheritExitsFrom(lhsExits);
		
		exits.inheritInlinksFrom(rhs);
		return exits;
	}

}
