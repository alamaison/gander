package uk.ac.ic.doc.gander.cfg.scope;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Compare;

import uk.ac.ic.doc.gander.cfg.scope.Statement.Exit;

public class CompareScope extends ScopeWithParent {

	private Compare node;

	public CompareScope(Compare node, Statement previousStatement,
			Exit trajectory, boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() {
		SimpleNode[] nodes = new SimpleNode[node.comparators.length + 1];
		nodes[0] = node.left;
		System.arraycopy(node.comparators, 0, nodes, 1, node.comparators.length);
		return delegate(nodes);
	}

}
