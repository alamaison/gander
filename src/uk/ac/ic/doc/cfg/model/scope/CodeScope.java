package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.AugAssign;
import org.python.pydev.parser.jython.ast.Break;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Compare;
import org.python.pydev.parser.jython.ast.Continue;
import org.python.pydev.parser.jython.ast.Expr;
import org.python.pydev.parser.jython.ast.For;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.If;
import org.python.pydev.parser.jython.ast.Return;
import org.python.pydev.parser.jython.ast.Str;
import org.python.pydev.parser.jython.ast.While;
import org.python.pydev.parser.jython.ast.Yield;

public abstract class CodeScope extends Scope {

	public CodeScope() {
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
	public Object visitFunctionDef(FunctionDef node) throws Exception {
		return delegateScope(new FunctionDefScope(node));
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
}