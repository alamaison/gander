package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Break;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Continue;
import org.python.pydev.parser.jython.ast.Expr;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.If;
import org.python.pydev.parser.jython.ast.While;

public abstract class CodeScope extends Scope {

	public CodeScope() {
		super();
	}

	private ScopeExits delegateScope(Scope scope) throws Exception {
		return scope.process();
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
		return delegateScope(new AssignScope(node, getCurrentBlock(), this));
	}

	@Override
	public Object visitExpr(Expr node) throws Exception {
		return delegateScope(new ExprScope(node, getCurrentBlock(), this));
	}

	@Override
	public Object visitCall(Call node) throws Exception {
		return delegateScope(new CallScope(node, getCurrentBlock(), this));
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
}