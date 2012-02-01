package uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow;

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
import org.python.pydev.parser.jython.ast.comprehensionType;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.model.Argument;
import uk.ac.ic.doc.gander.model.KeywordArgument;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.PositionalArgument;

final class FlowSituationFinder {

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
		expressionSite.codeObject().ast().accept(finder);
		return finder.getSituations();
	}
}

/**
 * AST visitor finding an expression's flow situations.
 */
final class SituationFinder extends VisitorBase {

	private final SituationMapper mapper;

	private final Set<FlowSituation> situations = new HashSet<FlowSituation>();

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

	@Override
	public Object visitAssert(Assert node) throws Exception {
		/* Nothing flows out of an assertion */
		return notInAFlowSituation();
	}

	@Override
	public Object visitAssign(Assign node) throws Exception {
		if (isMatch(node.value))
			return new AssignmentSituation(node, expression);
		else
			return notInAFlowSituation();
	}

	@Override
	public Object visitAttribute(Attribute node) throws Exception {
		if (isMatch(node)) {
			return new AttributeSituation(nodeToSite(node));
		} else if (isMatch(node.value)) {
			/*
			 * In reality, attributes accesses cause the value of the LHS to
			 * flow to the method wrapper if the RHS is a method. In that
			 * situations, the LHS's value flows to the first parameter of the
			 * method when it is called. But we don't model that.
			 * 
			 * Instead we capture results of constructor calls in visitCall and
			 * flow that value to the self parameter of each method.
			 */
			return notInAFlowSituation();
		} else {
			return notInAFlowSituation();
		}
	}

	@Override
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

	@Override
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

	@Override
	public Object visitBoolOp(BoolOp node) throws Exception {
		for (exprType value : node.values) {
			if (isMatch(value)) {
				return notInAFlowSituation(); // TODO
			}
		}
		return notInAFlowSituation();
	}

	@Override
	public Object visitBreak(Break node) throws Exception {
		return notInAFlowSituation();
	}

	@Override
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
			return new CallResultSituation(nodeToSite(node));
		} else if (node.func instanceof Attribute
				&& isMatch(((Attribute) node.func).value)) {
			/*
			 * Normally calling an expression doesn't cause its value to flow
			 * anywhere but there is a special case where the expression is an
			 * attribute. If that attribute turns out to be a bound method then
			 * the value of the LHS of the attribute flows to the first
			 * parameter of the method.
			 * 
			 * Note: this doesn't happen for just any call to an attribute; only
			 * for those that are bound methods.
			 */
			ModelSite<Attribute> receiver = new ModelSite<Attribute>(
					(Attribute) node.func, expression.codeObject());
			return new AttributeCallReceiverSituation(receiver);
		} else if (isMatch(node.starargs) || isMatch(node.kwargs)) {
			/*
			 * Unpacking an iterable, either through *args or *kwargs doesn't
			 * cause the iterable to flow any further; only its contents.
			 */
			return notInAFlowSituation();
		} else {

			/*
			 * Passing the expression as an argument in a call flows it to the
			 * corresponding parameter of the call receiver.
			 */

			for (int i = 0; i < node.args.length; ++i) {
				if (isMatch(node.args[i])) {
					
					ModelSite<Call> callSite = nodeToSite(node);
					Argument argument = new PositionalArgument(callSite, i);
					
					return new CallArgumentSituation(callSite, argument);
				}
			}

			for (int i = 0; i < node.keywords.length; ++i) {
				if (isMatch(node.keywords[i].value)) {
					
					ModelSite<Call> callSite = nodeToSite(node);
					Argument argument = new KeywordArgument(callSite, i);
					
					return new CallArgumentSituation(callSite, argument);
				}
			}

			return notInAFlowSituation();
		}
	}

	@Override
	public Object visitClassDef(ClassDef node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
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

	@Override
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

	@Override
	public Object visitContinue(Continue node) throws Exception {
		return notInAFlowSituation();
	}

	@Override
	public Object visitDelete(Delete node) throws Exception {
		for (exprType target : node.targets) {
			if (isMatch(target)) {
				return notInAFlowSituation(); // TODO
			}
		}
		return notInAFlowSituation();
	}

	@Override
	public Object visitDict(Dict node) throws Exception {

		/*
		 * Using an expression in a dictionary literal flows that expression's
		 * value into the dictionary. Unfortunately we can't track this flow so
		 * we model it as escaping to all possible flow positions.
		 */

		for (exprType key : node.keys) {
			if (isMatch(key)) {
				return EscapeSituation.INSTANCE;
			}
		}
		for (exprType value : node.values) {
			if (isMatch(value)) {
				return EscapeSituation.INSTANCE;
			}
		}
		return notInAFlowSituation();
	}

	@Override
	public Object visitDictComp(DictComp node) throws Exception {
		/* TODO: only in Python 2.7+ */
		return notInAFlowSituation();
	}

	@Override
	public Object visitEllipsis(Ellipsis node) throws Exception {
		return notInAFlowSituation();
	}

	@Override
	public Object visitExec(Exec node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitExpr(Expr node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitExpression(Expression node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitExtSlice(ExtSlice node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitFor(For node) throws Exception {
		/*
		 * If the for loop is passed an iterable the escape is handled by the
		 * node wherever that iterable is created. If it is passed a
		 * comma-delimited list, they appear as a tuple in the AST so the escape
		 * is handled when traversing into that.
		 */
		return notInAFlowSituation();
	}

	@Override
	public Object visitFunctionDef(FunctionDef node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitGeneratorExp(GeneratorExp node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitGlobal(Global node) throws Exception {
		return notInAFlowSituation();
	}

	@Override
	public Object visitIf(If node) throws Exception {
		return notInAFlowSituation();
	}

	@Override
	public Object visitIfExp(IfExp node) throws Exception {
		if (isMatch(node.test)) {
			return notInAFlowSituation();
		} else if (isMatch(node.body)) {
			return new ExpressionSituation(nodeToSite(node));
		} else if (isMatch(node.orelse)) {
			return new ExpressionSituation(nodeToSite(node));
		} else {
			return notInAFlowSituation();
		}
	}

	@Override
	public Object visitImport(Import node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitImportFrom(ImportFrom node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitIndex(Index node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitInteractive(Interactive node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitLambda(Lambda node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitList(List node) throws Exception {
		for (exprType value : node.elts) {
			if (isMatch(value)) {
				/*
				 * Using an expression in a list literal flows that expression's
				 * value into the list. Unfortunately we can't track this flow
				 * so we model it as escaping to all possible flow positions.
				 */
				return EscapeSituation.INSTANCE;
			}
		}
		return notInAFlowSituation();
	}

	@Override
	public Object visitListComp(ListComp node) throws Exception {
		for (comprehensionType gen : node.generators) {
			/*
			 * FIXME: BUGBUGBUG
			 * 
			 * There is a bug in the generated AST that makes the following list
			 * comprehensions indistinguishable:
			 * 
			 * [i for i in x, y]
			 * 
			 * [i for i in x if y]
			 * 
			 * Both appear with y in the ifs member of the comprehension but
			 * they have very different meanings when executed. To be on the
			 * safe side, we must assume the first case is what we're seeing and
			 * infer Top if our expression appears anywhere within the ifs list
			 * or if it appears as the iterable (despite being *in* the
			 * iterable).
			 */
			Comprehension comprehension = (Comprehension) gen;
			if (isMatch(comprehension.iter)) {
				return EscapeSituation.INSTANCE;
			} else {
				for (exprType ifexpr : comprehension.ifs) {
					if (isMatch(ifexpr)) {
						return EscapeSituation.INSTANCE;
					}
				}
			}
		}
		return notInAFlowSituation();
	}

	@Override
	public Object visitModule(Module node) throws Exception {
		return notInAFlowSituation();
	}

	@Override
	public Object visitName(Name node) throws Exception {
		if (isMatch(node)) {
			return new NameSituation(node, expression);
		} else {
			return notInAFlowSituation();
		}
	}

	@Override
	public Object visitNameTok(NameTok node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitNonLocal(NonLocal node) throws Exception {
		return notInAFlowSituation();
	}

	@Override
	public Object visitNum(Num node) throws Exception {
		return notInAFlowSituation();
	}

	@Override
	public Object visitPass(Pass node) throws Exception {
		return notInAFlowSituation();
	}

	@Override
	public Object visitPrint(Print node) throws Exception {
		return notInAFlowSituation();
	}

	@Override
	public Object visitRaise(Raise node) throws Exception {
		/*
		 * Raising an exception flows the exception object to almost anywhere.
		 * Give up and return Top.
		 */

		/*
		 * The object being throw is the first expression if that is the only
		 * expression or the second expression if not ... roughly.
		 */
		if (node.cause == null && node.tback == null) {
			if (node.inst == null) {

				if (isMatch(node.type)) {
					return EscapeSituation.INSTANCE;
				}

			} else {

				if (isMatch(node.inst)) {
					return EscapeSituation.INSTANCE;
				}

			}
		}

		return notInAFlowSituation();
	}

	@Override
	public Object visitRepr(Repr node) throws Exception {
		return notInAFlowSituation();
	}

	@Override
	public Object visitReturn(Return node) throws Exception {
		if (isMatch(node.value)) {
			return new ReturnSituation(expression);
		} else {
			return notInAFlowSituation();
		}
	}

	@Override
	public Object visitSet(org.python.pydev.parser.jython.ast.Set node)
			throws Exception {
		for (exprType value : node.elts) {
			if (isMatch(value)) {
				/*
				 * Using an expression in a set literal flows that expression's
				 * value into the set. Unfortunately we can't track this flow so
				 * we model it as escaping to all possible flow positions.
				 */
				return EscapeSituation.INSTANCE;
			}
		}
		return notInAFlowSituation();
	}

	@Override
	public Object visitSetComp(SetComp node) throws Exception {
		/* TODO: only in Python 2.7+ */
		return notInAFlowSituation();
	}

	@Override
	public Object visitSlice(Slice node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitStarred(Starred node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitStr(Str node) throws Exception {
		return notInAFlowSituation();
	}

	@Override
	public Object visitStrJoin(StrJoin node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitSubscript(Subscript node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitSuite(Suite node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitTryExcept(TryExcept node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitTryFinally(TryFinally node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitTuple(Tuple node) throws Exception {
		/*
		 * A single-element tuple without a trailing comma is not a tuple; it's
		 * just a pair of brackets! so whatever expression flowed into the
		 * tuple, flows straight back out.
		 */
		if (node.elts.length == 1 && !node.endsWithComma) {
			if (isMatch(node.elts[0])) {
				return new ExpressionSituation(nodeToSite(node));
			}
		} else {
			for (exprType value : node.elts) {
				if (isMatch(value)) {
					/*
					 * Using an expression in a tuple literal flows that
					 * expression's value into the tuple. Unfortunately we can't
					 * track this flow so we model it as escaping to all
					 * possible flow positions.
					 */
					return EscapeSituation.INSTANCE;
				}
			}
		}

		return notInAFlowSituation();
	}

	@Override
	public Object visitUnaryOp(UnaryOp node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitWhile(While node) throws Exception {
		return notInAFlowSituation();
	}

	@Override
	public Object visitWith(With node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitWithItem(WithItem node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	@Override
	public Object visitYield(Yield node) throws Exception {
		// TODO Auto-generated method stub
		return notInAFlowSituation();
	}

	private boolean isMatch(exprType otherExpression) {
		return expression.astNode().equals(otherExpression);
	}

	private <T extends exprType> ModelSite<T> nodeToSite(T node) {
		return new ModelSite<T>(node, expression.codeObject());
	}

	private Object notInAFlowSituation() {
		return null;
	}
}
