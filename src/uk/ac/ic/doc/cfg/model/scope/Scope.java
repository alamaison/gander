package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.VisitorBase;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public abstract class Scope extends VisitorBase {

	private BasicBlock block = null;

	@Override
	public void traverse(SimpleNode node) throws Exception {
		node.traverse(this);
	}

	protected abstract ScopeExits process() throws Exception;

	protected abstract BasicBlock newBlock();

	@Override
	protected Object unhandled_node(SimpleNode node) throws Exception {
		System.err.println("Unhandled node: " + node);
		ScopeExits exits = new ScopeExits();
		exits.setRoot(getCurrentBlock());
		return exits;
	}

	protected BasicBlock getCurrentBlock() {
		return block;
	}

	protected void setCurrentBlock(BasicBlock block) {
		this.block = block;
	}
}