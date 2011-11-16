package uk.ac.ic.doc.gander.flowinference.flowgoals.flowsituations;

import java.util.HashSet;
import java.util.Set;

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

import uk.ac.ic.doc.gander.model.ModelSite;

public final class FlowSituationFinder {

	/**
	 * Search expression's enclosing namespace, which must contain our
	 * expression, to find the 'situations' it finds itself in.
	 * 
	 * @return the flow situations or {@code null} if the expression is not in a
	 *         situations that leads to further flow.
	 */
	public static Set<FlowSituation> findFlowSituations(
			final ModelSite<? extends exprType> expressionSite) {

		if (expressionSite == null)
			throw new NullPointerException(
					"Need expression to find its flow situations");

		Set<FlowSituation> situation;
		try {
			situation = search(expressionSite);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return situation;
	}

	private static Set<FlowSituation> search(
			ModelSite<? extends exprType> expressionSite) throws Exception {

		SituationFinder finder = new SituationFinder(expressionSite);
		expressionSite.codeObject().getAst().accept(finder);
		return finder.getSituations();
	}
}

/**
 * AST visitor finding an expression's flow situations.
 */
final class SituationFinder extends VisitorBase {

	private final SituationMapper mapper;

	private Set<FlowSituation> situations = new HashSet<FlowSituation>();

	public SituationFinder(ModelSite<? extends exprType> expressionSite) {
		this.mapper = new SituationMapper(expressionSite);
	}

	Set<FlowSituation> getSituations() {
		return situations;
	}

	@Override
	protected Object unhandled_node(SimpleNode node) throws Exception {
		FlowSituation situation = (FlowSituation) node.accept(mapper);
		if (situation != null) {
			situations.add(situation);
		}
		return null;
	}

	@Override
	public void traverse(SimpleNode node) throws Exception {
		/*
		 * A single expression can be in multiple flow situations at different
		 * depths so we need to traverse to catch them all.
		 */
		node.traverse(this);
	}
}

/**
 * Converting the node that accepts it into the flow situation that the given
 * expression finds itself in.
 * 
 * If the node does represent such a situation (or if the expression is not
 * present in the node at all), this class does not traverse any further. It
 * just returns {@code null} allowing the caller to handle any further search.
 */
final class SituationMapper implements VisitorIF {

	private final ModelSite<? extends exprType> expression;

	SituationMapper(ModelSite<? extends exprType> expression) {
		this.expression = expression;
	}

	public Object visitAssert(Assert node) throws Exception {
		/* Nothing flows out of an assertion */
		return notInAFlowSituation();
	}

	public Object visitAssign(Assign node) throws Exception {
		if (isMatch(node.value))
			return new AssignmentSituation(node, expression);
		else
			return notInAFlowSituation();
	}

	public Object visitAttribute(Attribute node) throws Exception {
		/*
		 * In reality, attributes accesses cause the value of the LHS to flow to
		 * the method wrapper if the RHS is a method. In that situations, the
		 * LHS's value flows to the first parameter of the method when it is
		 * called. But we don't model that.
		 * 
		 * Instead we capture results of constructor calls in visitCall and flow
		 * that value to the self parameter of each method.
		 */
		return notInAFlowSituation();
	}

	public Object visitAugAssign(AugAssign node) throws Exception {
		// TODO Can this flow the value? For instance does it add
		// a value to a list?
		if (isMatch(node.target)) {
			return notInAFlowSituation(); // TODO
		} else if (isMatch(node.value)) {
			return notInAFlowSituation(); // TODO
		} else {
			return notInAFlowSituation();
		}
	}

	public Object visitBinOp(BinOp node) throws Exception {
		// TODO Can this flow the value? For instance does it add
		// a value to a list?
		if (isMatch(node.left)) {
			return notInAFlowSituation(); // TODO
		} else if (isMatch(node.right)) {
			return notInAFlowSituation(); // TODO
		} else {
			return notInAFlowSituation();
		}
	}

	public Object visitBoolOp(BoolOp node) throws Exception {
		for (exprType value : node.values) {
			if (isMatch(value)) {
				return notInAFlowSituation(); // TODO
			}
		}
		return notInAFlowSituation();
	}

	public Object visitBreak(Break node) throws Exception {
		return notInAFlowSituation();
	}

	public Object visitCall(Call node) throws Exception {
		/*
		 * An expression appearing in a call node can flow further depending on
		 * what part it plays in the call.
		 * 
		 * A callable object's value doesn't flow any further by virtue of being
		 * called:
		 * 
		 * - If the callable is a method, the method's value doesn't flow; the
		 * *object's* value flows but that is handled separately above.
		 * 
		 * - If the callable is a function, the function's value doesn't flow as
		 * a result of the call. It may flow through assignment, etc., but that
		 * is handled elsewhere.
		 * 
		 * - TODO: Handle callable objects. In this case, the callable's value
		 * does appear to flow to the 'self' parameter of the called method as a
		 * call to the object 'obj()' is shorthand for 'obj.__call__()'.
		 * 
		 * When the expression is the receiver of a method call, its value flows
		 * to the 'self' parameter of the method being called. But we don't
		 * model that. Instead we capture results of constructor calls below and
		 * flow that value to the self parameter of each method.
		 */

		if (isMatch(node)) {
			/*
			 * The expression in question is the result of a call.
			 * 
			 * The result of a call can flow onwards in two ways:
			 * 
			 * - by binding it to another expression such as by assignment; this
			 * is handled in the situation mapper for the appropriate binding
			 * node
			 * 
			 * - if the function being called was a constructor, the value
			 * flowed to the 'self' parameter of its methods; this is handled
			 * here
			 */
			return new CallResultSituation(expression);
		} else {
			return notInAFlowSituation();
		}
	}

	public Object visitClassDef(ClassDef node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitCompare(Compare node) throws Exception {
		if (isMatch(node.left)) {
			return notInAFlowSituation(); // TODO
		} else {
			for (exprType comparator : node.comparators) {
				if (isMatch(comparator)) {
					return notInAFlowSituation(); // TODO
				}
			}
			return notInAFlowSituation();
		}
	}

	public Object visitComprehension(Comprehension node) throws Exception {
		if (isMatch(node.target)) {
			return notInAFlowSituation(); // TODO
		} else if (isMatch(node.iter)) {
			return notInAFlowSituation(); // TODO
		} else {
			for (exprType arg : node.ifs) {
				if (isMatch(arg)) {
					return notInAFlowSituation(); // TODO
				}
			}
			return notInAFlowSituation();
		}
	}

	public Object visitContinue(Continue node) throws Exception {
		return notInAFlowSituation();
	}

	public Object visitDelete(Delete node) throws Exception {
		for (exprType target : node.targets) {
			if (isMatch(target)) {
				return notInAFlowSituation(); // TODO
			}
		}
		return notInAFlowSituation();
	}

	public Object visitDict(Dict node) throws Exception {
		for (exprType key : node.keys) {
			if (isMatch(key)) {
				return notInAFlowSituation(); // TODO
			}
		}
		for (exprType value : node.values) {
			if (isMatch(value)) {
				return notInAFlowSituation(); // TODO
			}
		}
		return notInAFlowSituation();
	}

	public Object visitDictComp(DictComp node) throws Exception {
		if (isMatch(node.key)) {
			return notInAFlowSituation(); // TODO
		} else if (isMatch(node.value)) {
			return notInAFlowSituation(); // TODO
		} else {
			// XXX: not sure how to handle comprehensions here
			// for (comprehensionType arg : node.generators) {
			return notInAFlowSituation();
		}
	}

	public Object visitEllipsis(Ellipsis node) throws Exception {
		return notInAFlowSituation();
	}

	public Object visitExec(Exec node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitExpr(Expr node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitExpression(Expression node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitExtSlice(ExtSlice node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitFor(For node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitFunctionDef(FunctionDef node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitGeneratorExp(GeneratorExp node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitGlobal(Global node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitIf(If node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitIfExp(IfExp node) throws Exception {
		if (isMatch(node.test)) {
			return notInAFlowSituation(); // TODO
		} else if (isMatch(node.body)) {
			return notInAFlowSituation(); // TODO
		} else if (isMatch(node.orelse)) {
			return notInAFlowSituation(); // TODO
		} else {
			return notInAFlowSituation();
		}

		// situations[0] = new ExpressionSituation(new ModelSite<IfExp>(node,
		// site
		// .getEnclosingScope(), model));

	}

	public Object visitImport(Import node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitImportFrom(ImportFrom node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitIndex(Index node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitInteractive(Interactive node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitLambda(Lambda node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitList(List node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitListComp(ListComp node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitModule(Module node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitName(Name node) throws Exception {
		if (isMatch(node)) {
			return new NameSituation(node, expression);
		} else {
			return notInAFlowSituation();
		}
	}

	public Object visitNameTok(NameTok node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitNonLocal(NonLocal node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitNum(Num node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitPass(Pass node) throws Exception {
		return notInAFlowSituation();
	}

	public Object visitPrint(Print node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitRaise(Raise node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitRepr(Repr node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitReturn(Return node) throws Exception {
		if (isMatch(node.value)) {
			return new ReturnSituation(expression);
		} else {
			return notInAFlowSituation();
		}
	}

	public Object visitSet(org.python.pydev.parser.jython.ast.Set node)
			throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitSetComp(SetComp node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitSlice(Slice node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitStarred(Starred node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitStr(Str node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitStrJoin(StrJoin node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitSubscript(Subscript node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitSuite(Suite node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitTryExcept(TryExcept node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitTryFinally(TryFinally node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitTuple(Tuple node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitUnaryOp(UnaryOp node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitWhile(While node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitWith(With node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitWithItem(WithItem node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	public Object visitYield(Yield node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	private boolean isMatch(exprType otherExpression) {
		return expression.astNode().equals(otherExpression);
	}

	private Object notInAFlowSituation() {
		return null;
	}
}
