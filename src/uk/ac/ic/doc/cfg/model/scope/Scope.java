package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.AugAssign;
import org.python.pydev.parser.jython.ast.BoolOp;
import org.python.pydev.parser.jython.ast.Break;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.Compare;
import org.python.pydev.parser.jython.ast.Continue;
import org.python.pydev.parser.jython.ast.Expr;
import org.python.pydev.parser.jython.ast.For;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.If;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.Pass;
import org.python.pydev.parser.jython.ast.Raise;
import org.python.pydev.parser.jython.ast.Return;
import org.python.pydev.parser.jython.ast.Str;
import org.python.pydev.parser.jython.ast.TryExcept;
import org.python.pydev.parser.jython.ast.TryFinally;
import org.python.pydev.parser.jython.ast.UnaryOp;
import org.python.pydev.parser.jython.ast.VisitorBase;
import org.python.pydev.parser.jython.ast.While;
import org.python.pydev.parser.jython.ast.Yield;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public abstract class Scope extends VisitorBase {

	private BasicBlock block = null;

	public Scope() {
		super();
	}

	private ScopeExits delegateScope(Scope scope) throws Exception {
		return scope.process();
	}

	private <T extends SimpleNode> ScopeExits delegateSelfAddingScope(T node)
			throws Exception {
		return delegateScope(new SelfAddingScope<T>(node, getCurrentBlock(),
				this));
	}

	@Override
	public Object visitIf(If node) throws Exception {
		return delegateScope(new IfScope(node, getCurrentBlock(), this));
	}

	@Override
	public Object visitWhile(While node) throws Exception {
		return delegateScope(new WhileScope(node, getCurrentBlock(), this));
	}

	@Override
	public Object visitAssign(Assign node) throws Exception {
		return delegateSelfAddingScope(node);
	}

	@Override
	public Object visitExpr(Expr node) throws Exception {
		return delegateScope(new ExprScope(node, getCurrentBlock(), this));
	}

	@Override
	public Object visitCall(Call node) throws Exception {
		return delegateSelfAddingScope(node);
	}

	@Override
	public Object visitBreak(Break node) throws Exception {
		return delegateScope(new BreakScope(node, getCurrentBlock(), this));
	}

	@Override
	public Object visitContinue(Continue node) throws Exception {
		return delegateScope(new ContinueScope(node, getCurrentBlock(), this));
	}

	@Override
	public Object visitReturn(Return node) throws Exception {
		return delegateScope(new ReturnScope(node, getCurrentBlock(), this));
	}

	@Override
	public Object visitFor(For node) throws Exception {
		return delegateScope(new ForScope(node, getCurrentBlock(), this));
	}

	@Override
	public Object visitYield(Yield node) throws Exception {
		return delegateScope(new YieldScope(node, getCurrentBlock(), this));
	}

	@Override
	public Object visitAugAssign(AugAssign node) throws Exception {
		return delegateSelfAddingScope(node);
	}

	@Override
	public Object visitStr(Str node) throws Exception {
		return delegateSelfAddingScope(node);
	}

	@Override
	public Object visitCompare(Compare node) throws Exception {
		return delegateSelfAddingScope(node);
	}

	@Override
	public Object visitName(Name node) throws Exception {
		return delegateSelfAddingScope(node);
	}

	@Override
	public Object visitAttribute(Attribute node) throws Exception {
		return delegateSelfAddingScope(node);
	}

	@Override
	public Object visitBoolOp(BoolOp node) throws Exception {
		// TODO: pull referenced variables out of node
		return delegateSelfAddingScope(node);
	}

	@Override
	public Object visitUnaryOp(UnaryOp node) throws Exception {
		// TODO: pull referenced variables out of node
		return delegateSelfAddingScope(node);
	}

	@Override
	public Object visitFunctionDef(FunctionDef node) throws Exception {
		// We don't analyse flow in nested function definitions.  Just
		// add AST as-is to control graph so we can detect presence of
		// token and treat like a local variable.
		return delegateSelfAddingScope(node);
	}

	@Override
	public Object visitClassDef(ClassDef node) throws Exception {
		// We don't analyse flow in nested class definitions.  Just
		// add AST as-is to control graph so we can detect presence of
		// token and treat like a local variable.
		return delegateSelfAddingScope(node);
	}

	@Override
	public Object visitRaise(Raise node) throws Exception {
		return delegateScope(new RaiseScope(node, getCurrentBlock(), this));
	}

	@Override
	public Object visitPass(Pass node) throws Exception {
		return delegateScope(new PassScope(node, getCurrentBlock(), this));
	}

	@Override
	public Object visitTryExcept(TryExcept node) throws Exception {
		return delegateScope(new TryExceptScope(node, getCurrentBlock(), this));
	}

	@Override
	public Object visitTryFinally(TryFinally node) throws Exception {
		return delegateScope(new TryFinallyScope(node, getCurrentBlock(), this));
	}

	@Override
	public void traverse(SimpleNode node) throws Exception {
		node.traverse(this);
	}

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

	protected abstract ScopeExits process() throws Exception;

	protected abstract BasicBlock newBlock();
}