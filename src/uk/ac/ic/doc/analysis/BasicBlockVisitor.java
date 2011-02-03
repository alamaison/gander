package uk.ac.ic.doc.analysis;

import org.python.pydev.parser.jython.ast.Assert;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.AugAssign;
import org.python.pydev.parser.jython.ast.BinOp;
import org.python.pydev.parser.jython.ast.BoolOp;
import org.python.pydev.parser.jython.ast.Break;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.Compare;
import org.python.pydev.parser.jython.ast.Comprehension;
import org.python.pydev.parser.jython.ast.Continue;
import org.python.pydev.parser.jython.ast.Delete;
import org.python.pydev.parser.jython.ast.Dict;
import org.python.pydev.parser.jython.ast.DictComp;
import org.python.pydev.parser.jython.ast.Ellipsis;
import org.python.pydev.parser.jython.ast.Exec;
import org.python.pydev.parser.jython.ast.Expr;
import org.python.pydev.parser.jython.ast.Expression;
import org.python.pydev.parser.jython.ast.ExtSlice;
import org.python.pydev.parser.jython.ast.For;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.GeneratorExp;
import org.python.pydev.parser.jython.ast.Global;
import org.python.pydev.parser.jython.ast.If;
import org.python.pydev.parser.jython.ast.IfExp;
import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.Index;
import org.python.pydev.parser.jython.ast.Interactive;
import org.python.pydev.parser.jython.ast.Lambda;
import org.python.pydev.parser.jython.ast.List;
import org.python.pydev.parser.jython.ast.ListComp;
import org.python.pydev.parser.jython.ast.Module;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.NonLocal;
import org.python.pydev.parser.jython.ast.Num;
import org.python.pydev.parser.jython.ast.Pass;
import org.python.pydev.parser.jython.ast.Print;
import org.python.pydev.parser.jython.ast.Raise;
import org.python.pydev.parser.jython.ast.Repr;
import org.python.pydev.parser.jython.ast.Return;
import org.python.pydev.parser.jython.ast.Set;
import org.python.pydev.parser.jython.ast.SetComp;
import org.python.pydev.parser.jython.ast.Slice;
import org.python.pydev.parser.jython.ast.Starred;
import org.python.pydev.parser.jython.ast.Str;
import org.python.pydev.parser.jython.ast.StrJoin;
import org.python.pydev.parser.jython.ast.Subscript;
import org.python.pydev.parser.jython.ast.Suite;
import org.python.pydev.parser.jython.ast.TryExcept;
import org.python.pydev.parser.jython.ast.TryFinally;
import org.python.pydev.parser.jython.ast.Tuple;
import org.python.pydev.parser.jython.ast.UnaryOp;
import org.python.pydev.parser.jython.ast.VisitorBase;
import org.python.pydev.parser.jython.ast.While;
import org.python.pydev.parser.jython.ast.With;
import org.python.pydev.parser.jython.ast.WithItem;
import org.python.pydev.parser.jython.ast.Yield;

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
