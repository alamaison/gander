package uk.ac.ic.doc.gander.cfg.scope;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.cfg.scope.Statement.Exit;

public class CallScope extends ScopeWithParent {

	private Call node;
	
	public CallScope(Call node, Statement previousStatement, Exit trajectory,
			boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() throws Exception {
		addToCurrentBlock(node);
		
		Statement exits = new Statement();
		exits.inheritInlinksFrom(previousStatement());
		exits.inheritFallthroughsFrom(previousStatement());
		//exits.convertFallthroughsToRaises(previousStatement());
		
		return exits;
	}

}
