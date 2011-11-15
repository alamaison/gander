package uk.ac.ic.doc.gander.flowinference.flowgoals.flowsituations;

import org.python.pydev.parser.jython.SimpleNode;
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
import org.python.pydev.parser.jython.ast.VisitorIF;
import org.python.pydev.parser.jython.ast.While;
import org.python.pydev.parser.jython.ast.With;
import org.python.pydev.parser.jython.ast.WithItem;
import org.python.pydev.parser.jython.ast.Yield;
import org.python.pydev.parser.jython.ast.exprType;
import org.python.pydev.parser.jython.ast.keywordType;

import uk.ac.ic.doc.gander.model.ModelSite;

public final class FlowSituationFinder {

	/**
	 * Search expression's enclosing namespace, which must contain our
	 * expression, to find the 'situation' it finds itself in.
	 * 
	 * @return the flow situation or {@code null} if the expression is not in a
	 *         situation that leads to further flow.
	 */
	public static FlowSituation findFlowSituation(
			final ModelSite<?> expressionSite) {

		if (expressionSite == null)
			throw new NullPointerException(
					"Need expression to find its flow situation");

		FlowSituation situation;
		try {
			situation = search(expressionSite);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return situation;
	}

	private static FlowSituation search(final ModelSite<?> expressionSite)
			throws Exception {

		SituationFinder finder = new SituationFinder(expressionSite);
		expressionSite.codeObject().getAst().accept(finder);
		return finder.getSituation();
	}
}

/**
 * AST visitor finding an expression's flow situation.
 */
final class SituationFinder extends VisitorBase {

	private FlowSituation situation = null;
	private final SituationMapper mapper;

	public SituationFinder(ModelSite<?> expressionSite) {
		this.mapper = new SituationMapper(expressionSite);
	}

	FlowSituation getSituation() {
		return situation;
	}

	@Override
	protected Object unhandled_node(SimpleNode node) throws Exception {
		if (situation == null)
			situation = (FlowSituation) node.accept(mapper);

		/*
		 * Even if this node doesn't match a flow-giving situation for the
		 * expression in question, a sub-node of this one might so we need to
		 * recurse into it.
		 */
		if (situation == null)
			node.traverse(this);

		return null;
	}

	@Override
	public void traverse(SimpleNode node) throws Exception {
		// Don't traverse. It is done in unhandled_node if necessary.
	}
}

/**
 * Responsible for converting the node that accepts it into a flow situation for
 * the expression it is constructed with.
 * 
 * If the node does represent such a situation (or if the expression is not
 * present in the node at all), this class does not traverse any further. It
 * just returns {@code null} allowing the caller to handle any further search.
 */
final class SituationMapper implements VisitorIF {

	private final ModelSite<?> expression;

	SituationMapper(ModelSite<?> expression) {
		this.expression = expression;
	}

	public Object visitAssert(Assert node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitAssign(Assign node) throws Exception {
		if (isMatch(node.value))
			return new AssignmentSituation(node, expression);
		else
			return null;
	}

	public Object visitAttribute(Attribute node) throws Exception {
		/*
		 * Attributes accesses cause the value of the LHS to flow onwards if the
		 * RHS is a method. In that situation, the LHS's value flows to the
		 * first parameter of the method.
		 */
		// if (isMatch(node.value))
		// return new AttributeSituation(new ModelSite<Attribute>(node,
		// expression.getEnclosingScope(), expression.getModel()));
		// else
		return null;
	}

	public Object visitAugAssign(AugAssign node) throws Exception {
		// TODO Can this flow the value? For instance does it add
		// a value to a list?
		if (isMatch(node.target)) {
			return null; // TODO
		} else if (isMatch(node.value)) {
			return null; // TODO
		} else {
			return null;
		}
	}

	public Object visitBinOp(BinOp node) throws Exception {
		// TODO Can this flow the value? For instance does it add
		// a value to a list?
		if (isMatch(node.left)) {
			return null; // TODO
		} else if (isMatch(node.right)) {
			return null; // TODO
		} else {
			return null;
		}
	}

	public Object visitBoolOp(BoolOp node) throws Exception {
		for (exprType value : node.values) {
			if (isMatch(value)) {
				return null; // TODO
			}
		}
		return null;
	}

	public Object visitBreak(Break node) throws Exception {
		return null;
	}

	public Object visitCall(Call node) throws Exception {
		if (isMatch(node.func)) {
			/*
			 * A callable object's value doesn't flow any further by virtue of
			 * being called.
			 * 
			 * If the callable is a method, the method's value doesn't flow; the
			 * *object's* value flows but that is handled separately below.
			 * 
			 * If the callable is a function, the function's value doesn't flow
			 * as a result of the call. It may flow through assignment, etc.,
			 * but that is handled elsewhere.
			 * 
			 * TODO: Handle callable object. In this case, the callable's value
			 * does appear to flow to the 'self' parameter of the called method
			 * as a call to the object 'obj()' is shorthand for
			 * 'obj.__call__()'.
			 */
			return null;
			// } else if (node.func instanceof Attribute
			// && isMatch(((Attribute) node.func).value)) {
			/*
			 * When the expression is the receiver of a method call, its value
			 * flows to the 'self' parameter of the method being called.
			 * 
			 * Method calls are a bit special from the perspective of mapping to
			 * a flow situation. We have to drill down more than one AST node
			 * level as the attribute being accessed is actually a closure that
			 * stores the receiver type in it but we don't model that at the
			 * moment so we have to pass the receiver this way.
			 * 
			 * TODO: This ignore the possibility that the method is called by
			 * closure. I think we need to distinguish bound and unbound methods
			 * for this.
			 */
			// return new CallRecieverSituation(node, expression);
		} else if (isMatch(node.kwargs)) {

			return null; // TODO
		} else if (isMatch(node.starargs)) {
			return null; // TODO
		} else {
			for (exprType arg : node.args) {
				if (isMatch(arg)) {
					return null; // TODO
				}
			}
			for (keywordType keyword : node.keywords) {
				if (isMatch(keyword.value)) {
					return null; // TODO
				}
			}
			return null;
		}
	}

	public Object visitClassDef(ClassDef node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitCompare(Compare node) throws Exception {
		if (isMatch(node.left)) {
			return null; // TODO
		} else {
			for (exprType comparator : node.comparators) {
				if (isMatch(comparator)) {
					return null; // TODO
				}
			}
			return null;
		}
	}

	public Object visitComprehension(Comprehension node) throws Exception {
		if (isMatch(node.target)) {
			return null; // TODO
		} else if (isMatch(node.iter)) {
			return null; // TODO
		} else {
			for (exprType arg : node.ifs) {
				if (isMatch(arg)) {
					return null; // TODO
				}
			}
			return null;
		}
	}

	public Object visitContinue(Continue node) throws Exception {
		return null;
	}

	public Object visitDelete(Delete node) throws Exception {
		for (exprType target : node.targets) {
			if (isMatch(target)) {
				return null; // TODO
			}
		}
		return null;
	}

	public Object visitDict(Dict node) throws Exception {
		for (exprType key : node.keys) {
			if (isMatch(key)) {
				return null; // TODO
			}
		}
		for (exprType value : node.values) {
			if (isMatch(value)) {
				return null; // TODO
			}
		}
		return null;
	}

	public Object visitDictComp(DictComp node) throws Exception {
		if (isMatch(node.key)) {
			return null; // TODO
		} else if (isMatch(node.value)) {
			return null; // TODO
		} else {
			// XXX: not sure how to handle comprehensions here
			// for (comprehensionType arg : node.generators) {
			return null;
		}
	}

	public Object visitEllipsis(Ellipsis node) throws Exception {
		return null;
	}

	public Object visitExec(Exec node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitExpr(Expr node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitExpression(Expression node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitExtSlice(ExtSlice node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitFor(For node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitFunctionDef(FunctionDef node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitGeneratorExp(GeneratorExp node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitGlobal(Global node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitIf(If node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitIfExp(IfExp node) throws Exception {
		if (isMatch(node.test)) {
			return null; // TODO
		} else if (isMatch(node.body)) {
			return null; // TODO
		} else if (isMatch(node.orelse)) {
			return null; // TODO
		} else {
			return null;
		}

		// situation[0] = new ExpressionSituation(new ModelSite<IfExp>(node,
		// site
		// .getEnclosingScope(), model));

	}

	public Object visitImport(Import node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitImportFrom(ImportFrom node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitIndex(Index node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitInteractive(Interactive node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitLambda(Lambda node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitList(List node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitListComp(ListComp node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitModule(Module node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitName(Name node) throws Exception {
		if (isMatch(node)) {
			return new NameSituation(node, expression);
		} else {
			return null;
		}
	}

	public Object visitNameTok(NameTok node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitNonLocal(NonLocal node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitNum(Num node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitPass(Pass node) throws Exception {
		return null;
	}

	public Object visitPrint(Print node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitRaise(Raise node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitRepr(Repr node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitReturn(Return node) throws Exception {
		if (isMatch(node.value)) {
			return new ReturnSituation(expression);
		} else {
			return null;
		}
	}

	public Object visitSet(Set node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitSetComp(SetComp node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitSlice(Slice node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitStarred(Starred node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitStr(Str node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitStrJoin(StrJoin node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitSubscript(Subscript node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitSuite(Suite node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitTryExcept(TryExcept node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitTryFinally(TryFinally node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitTuple(Tuple node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitUnaryOp(UnaryOp node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitWhile(While node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitWith(With node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitWithItem(WithItem node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visitYield(Yield node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isMatch(exprType otherExpression) {
		return expression.astNode().equals(otherExpression);
	}
}
