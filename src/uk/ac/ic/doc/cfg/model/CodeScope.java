package uk.ac.ic.doc.cfg.model;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Expr;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.If;
import org.python.pydev.parser.jython.ast.While;

public abstract class CodeScope extends Scope {

	Set<BasicBlock> fallthroughQueue = new HashSet<BasicBlock>();

	public CodeScope() {
		super();
	}
	
	private void delegateScope(Scope scope) throws Exception {
		
		// Blocks may be waiting for fall-through after processing a branch
		// or a loop.  If so, we must start a new basic block and link the
		// fall-through blocks to it.
		if (fallthroughQueue.size() > 0) {
			BasicBlock nextBlock = newBlock();
			for (BasicBlock b : fallthroughQueue) {
				b.link(nextBlock);
			}
			fallthroughQueue.clear();
			setCurrentBlock(nextBlock);
		}
		
		scope.setCurrentBlock(getCurrentBlock());
		scope.process();
		scope.end();
	}

	@Override
	public Object visitIf(If node) throws Exception {
		delegateScope(new IfScope(node, this));
		return null;
	}

	@Override
	public Object visitWhile(While node) throws Exception {
		delegateScope(new WhileScope(node, this));
		return null;
	}

	@Override
	public Object visitAssign(Assign node) throws Exception {
		delegateScope(new AssignScope(node, this));
		return null;
	}

	@Override
	public Object visitExpr(Expr node) throws Exception {
		delegateScope(new ExprScope(node, this));
		return null;
	}

	@Override
	public Object visitCall(Call node) throws Exception {
		delegateScope(new CallScope(node, this));
		return null;
	}
	
	@Override
	public Object visitFunctionDef(FunctionDef node) throws Exception {
		delegateScope(new FunctionDefScope(node));
		return null;
	}

	@Override
	protected void end() {}
	
	protected void fallthrough(BasicBlock predecessor) {
		fallthroughQueue.add(predecessor);
	}

}