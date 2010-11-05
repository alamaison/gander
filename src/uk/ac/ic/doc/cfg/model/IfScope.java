package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.ast.If;
import org.python.pydev.parser.jython.ast.Suite;

public class IfScope extends Scope {

	private BasicBlock thenBlock = new BasicBlock();
	private BasicBlock elseBlock = null;
	
	private FunctionDefScope parent;
		
	public IfScope(If node, FunctionDefScope parent) throws Exception {
		super();
		this.parent = parent;
		setCurrentBlock(thenBlock);
		node.accept(this);
	}

	@Override
	public Object visitIf(If node) throws Exception {
		Object ret = super.visitIf(node);
		finish();
		return ret;
	}

	@Override
	public Object visitSuite(Suite node) throws Exception {
		elseBlock = new BasicBlock();
		setCurrentBlock(elseBlock);
		return super.visitSuite(node);
	}

	@Override
	protected void finish() {
		parent.linkAfterCurrent(thenBlock);
		parent.linkFallthrough(thenBlock);
		if (elseBlock != null) {
			parent.linkAfterCurrent(elseBlock);
			parent.linkFallthrough(elseBlock);
		}
	}

}
