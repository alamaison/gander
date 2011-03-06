package uk.ac.ic.doc.gander.cfg.model.scope;

import org.python.pydev.parser.jython.ast.Print;

import uk.ac.ic.doc.gander.cfg.model.scope.Statement.Exit;

public class PrintScope extends ScopeWithParent {

	private Print node;

	public PrintScope(Print node, Statement previousStatement, Exit trajectory,
			boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() throws Exception {
		return delegate(node.values);
	}

}