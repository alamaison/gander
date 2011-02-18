package uk.ac.ic.doc.gander.analysis;

import org.python.pydev.parser.jython.ast.Break;
import org.python.pydev.parser.jython.ast.Continue;
import org.python.pydev.parser.jython.ast.Exec;
import org.python.pydev.parser.jython.ast.For;
import org.python.pydev.parser.jython.ast.If;
import org.python.pydev.parser.jython.ast.IfExp;
import org.python.pydev.parser.jython.ast.Interactive;
import org.python.pydev.parser.jython.ast.Module;
import org.python.pydev.parser.jython.ast.Raise;
import org.python.pydev.parser.jython.ast.Return;
import org.python.pydev.parser.jython.ast.TryExcept;
import org.python.pydev.parser.jython.ast.TryFinally;
import org.python.pydev.parser.jython.ast.VisitorBase;
import org.python.pydev.parser.jython.ast.While;
import org.python.pydev.parser.jython.ast.With;
import org.python.pydev.parser.jython.ast.WithItem;

public abstract class BasicBlockVisitor extends VisitorBase {

	private Object forbidden() {
		throw new Error("Control-flow changing statements not allowed in a basic block");
	}

	@Override
	public Object visitModule(Module node) throws Exception {
		return forbidden();
	}

	@Override
	public Object visitInteractive(Interactive node) throws Exception {
		return forbidden();
	}

	@Override
	public Object visitWithItem(WithItem node) throws Exception {
		return forbidden();
	}

	@Override
	public Object visitReturn(Return node) throws Exception {
		return forbidden();
	}

	@Override
	public Object visitFor(For node) throws Exception {
		return forbidden();
	}

	@Override
	public Object visitWhile(While node) throws Exception {
		return forbidden();
	}

	@Override
	public Object visitIf(If node) throws Exception {
		return forbidden();
	}

	@Override
	public Object visitWith(With node) throws Exception {
		return forbidden();
	}

	@Override
	public Object visitRaise(Raise node) throws Exception {
		return forbidden();
	}

	@Override
	public Object visitTryExcept(TryExcept node) throws Exception {
		return forbidden();
	}

	@Override
	public Object visitTryFinally(TryFinally node) throws Exception {
		return forbidden();
	}

	@Override
	public Object visitExec(Exec node) throws Exception {
		return forbidden();
	}

	@Override
	public Object visitBreak(Break node) throws Exception {
		return forbidden();
	}

	@Override
	public Object visitContinue(Continue node) throws Exception {
		return forbidden();
	}

	@Override
	public Object visitIfExp(IfExp node) throws Exception {
		return forbidden();
	}
}
