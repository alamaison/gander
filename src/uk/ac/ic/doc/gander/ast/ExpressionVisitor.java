package uk.ac.ic.doc.gander.ast;

import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.BinOp;
import org.python.pydev.parser.jython.ast.BoolOp;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Compare;
import org.python.pydev.parser.jython.ast.Comprehension;
import org.python.pydev.parser.jython.ast.Dict;
import org.python.pydev.parser.jython.ast.DictComp;
import org.python.pydev.parser.jython.ast.ExtSlice;
import org.python.pydev.parser.jython.ast.GeneratorExp;
import org.python.pydev.parser.jython.ast.IfExp;
import org.python.pydev.parser.jython.ast.Index;
import org.python.pydev.parser.jython.ast.Lambda;
import org.python.pydev.parser.jython.ast.List;
import org.python.pydev.parser.jython.ast.ListComp;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NonLocal;
import org.python.pydev.parser.jython.ast.Num;
import org.python.pydev.parser.jython.ast.Repr;
import org.python.pydev.parser.jython.ast.Set;
import org.python.pydev.parser.jython.ast.SetComp;
import org.python.pydev.parser.jython.ast.Slice;
import org.python.pydev.parser.jython.ast.Starred;
import org.python.pydev.parser.jython.ast.Str;
import org.python.pydev.parser.jython.ast.StrJoin;
import org.python.pydev.parser.jython.ast.Subscript;
import org.python.pydev.parser.jython.ast.Tuple;
import org.python.pydev.parser.jython.ast.UnaryOp;
import org.python.pydev.parser.jython.ast.VisitorBase;
import org.python.pydev.parser.jython.ast.Yield;

/**
 * Force subclass to handle every expression type.
 */
public abstract class ExpressionVisitor extends VisitorBase {

	@Override
	public abstract Object visitDict(Dict node) throws Exception;

	@Override
	public abstract Object visitList(List node) throws Exception;

	@Override
	public abstract Object visitNum(Num node) throws Exception;

	@Override
	public abstract Object visitSet(Set node) throws Exception;

	@Override
	public abstract Object visitStr(Str node) throws Exception;

	@Override
	public abstract Object visitAttribute(Attribute node) throws Exception;

	@Override
	public abstract Object visitBinOp(BinOp node) throws Exception;

	@Override
	public abstract Object visitBoolOp(BoolOp node) throws Exception;

	@Override
	public abstract Object visitStarred(Starred node) throws Exception;

	@Override
	public abstract Object visitUnaryOp(UnaryOp node) throws Exception;

	@Override
	public abstract Object visitYield(Yield node) throws Exception;

	@Override
	public abstract Object visitCall(Call node) throws Exception;

	@Override
	public abstract Object visitIfExp(IfExp node) throws Exception;

	@Override
	public abstract Object visitCompare(Compare node) throws Exception;

	@Override
	public abstract Object visitComprehension(Comprehension node)
			throws Exception;

	@Override
	public abstract Object visitDictComp(DictComp node) throws Exception;

	@Override
	public abstract Object visitExtSlice(ExtSlice node) throws Exception;

	@Override
	public abstract Object visitGeneratorExp(GeneratorExp node)
			throws Exception;

	@Override
	public abstract Object visitIndex(Index node) throws Exception;

	@Override
	public abstract Object visitLambda(Lambda node) throws Exception;

	@Override
	public abstract Object visitListComp(ListComp node) throws Exception;

	@Override
	public abstract Object visitName(Name node) throws Exception;

	@Override
	public abstract Object visitNonLocal(NonLocal node) throws Exception;

	@Override
	public abstract Object visitRepr(Repr node) throws Exception;

	@Override
	public abstract Object visitSetComp(SetComp node) throws Exception;

	@Override
	public abstract Object visitSlice(Slice node) throws Exception;

	@Override
	public abstract Object visitStrJoin(StrJoin node) throws Exception;

	@Override
	public abstract Object visitSubscript(Subscript node) throws Exception;

	@Override
	public abstract Object visitTuple(Tuple node) throws Exception;
}

