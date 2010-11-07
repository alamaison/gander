package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Expr;
import org.python.pydev.parser.jython.ast.VisitorBase;

public abstract class Scope extends VisitorBase {
	
	private BasicBlock block = null;
	
	protected void finish() {}
	
	protected void addToCurrentBlock(SimpleNode node) {
		if (block == null)
			block = new BasicBlock();
		
		assert getCurrentBlock().getOutSet().size() == 0;
		getCurrentBlock().addStatement(node);
	}

	@Override
	public Object visitAssign(Assign node) throws Exception {
		addToCurrentBlock(node);
		return super.visitAssign(node);
	}

	@Override
	public Object visitExpr(Expr node) throws Exception {
		addToCurrentBlock(node.value);
		return super.visitExpr(node);
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

}