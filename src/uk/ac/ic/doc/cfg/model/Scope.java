package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Expr;
import org.python.pydev.parser.jython.ast.VisitorBase;

public abstract class Scope extends VisitorBase {
	
	private BasicBlock block = null;
	
	protected void addToCurrentBlock(SimpleNode node) {
		if (block == null)
			block = new BasicBlock();
		
		assert getCurrentBlock().getOutSet().size() == 0;
		getCurrentBlock().addStatement(node);
	}

	@Override
	public Object visitAssign(Assign node) throws Exception {
		addToCurrentBlock(node);
		return null;
	}

	@Override
	public Object visitExpr(Expr node) throws Exception {
		addToCurrentBlock(node.value);
		return null;
	}

	@Override
	public Object visitCall(Call node) throws Exception {
		addToCurrentBlock(node);
		// TODO Make CallScope that handles exceptions killing the basic block
		return null;
	}


	@Override
	public void traverse(SimpleNode node) throws Exception {
		node.traverse(this);
	}

	@Override
	protected Object unhandled_node(SimpleNode node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	protected BasicBlock getCurrentBlock() {
		return block;
	}
	
	protected void setCurrentBlock(BasicBlock block) {
		this.block = block;
	}

	protected abstract void fallthrough(BasicBlock block);

	protected void linkAfterCurrent(BasicBlock successor) {
		getCurrentBlock().link(successor);
	}

}