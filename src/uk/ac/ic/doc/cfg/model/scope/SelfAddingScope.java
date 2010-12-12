package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.AugAssign;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class SelfAddingScope<T extends SimpleNode> extends ScopeWithParent {

	private T node;

	public SelfAddingScope(T node, BasicBlock root, Scope parent) {
		super(parent, root);
		this.node = node;
	}

	@Override
	protected ScopeExits doProcess() throws Exception {
		addToCurrentBlock(node);
		ScopeExits exits = new ScopeExits();
		exits.setRoot(getCurrentBlock());
		exits.fallthrough(getCurrentBlock());
		return exits;
	}
}
