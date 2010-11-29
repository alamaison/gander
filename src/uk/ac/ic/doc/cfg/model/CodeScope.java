package uk.ac.ic.doc.cfg.model;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Break;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Expr;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.If;
import org.python.pydev.parser.jython.ast.While;

public abstract class CodeScope extends Scope {

	protected Set<BasicBlock> fallthroughQueue = new HashSet<BasicBlock>();
	protected Set<BasicBlock> breakoutQueue = new HashSet<BasicBlock>();
	protected BasicBlock tail;

	public CodeScope() {
		super();
	}

	/**
	 * Blocks may be waiting for fall-through after processing a branch or a
	 * loop. If so, we must start a new basic block and link the fall-through
	 * blocks to it.
	 */
	private void knitFallthrough() {
		if (fallthroughQueue.size() > 0) {
			BasicBlock nextBlock = newBlock();
			for (BasicBlock b : fallthroughQueue) {
				b.link(nextBlock);
			}
			fallthroughQueue.clear();
			setCurrentBlock(nextBlock);
		}
	}

	private void delegateScope(Scope scope) throws Exception {
		knitFallthrough();
		scope.process();
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
	public Object visitBreak(Break node) throws Exception {
		delegateScope(new BreakScope(node, this));
		return null;
	}

	@Override
	protected void fallthrough(BasicBlock predecessor) {
		assert predecessor != null;
		fallthroughQueue.add(predecessor);
	}

	@Override
	protected void breakout(BasicBlock predecessor) {
		breakoutQueue.add(predecessor);
	}

	@Override
	protected void tail(BasicBlock block) {
		this.tail = block;
	}
}