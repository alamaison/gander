package uk.ac.ic.doc.gander.flowinference.typegoals;

import org.python.pydev.parser.jython.SimpleNode;
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
import org.python.pydev.parser.jython.ast.Yield;
import org.python.pydev.parser.jython.ast.exprType;
import org.python.pydev.parser.jython.ast.num_typeType;

import uk.ac.ic.doc.gander.ast.ExpressionVisitor;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.Top;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Namespace;

public final class ExpressionTypeGoal implements TypeGoal<TypeJudgement> {

	private final exprType expression;
	private final Model model;
	private final Namespace scope;

	public ExpressionTypeGoal(Model model, Namespace scope, exprType expression) {
		this.model = model;
		this.scope = scope;
		this.expression = expression;
	}

	public TypeJudgement initialSolution() {
		return SetBasedTypeJudgement.BOTTOM;
	}

	public TypeJudgement recalculateSolution(SubgoalManager goalManager) {
		TypeFinder finder = new TypeFinder(model, scope, goalManager);
		try {
			return (TypeJudgement) expression.accept(finder);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Visitor that determines the type of the expression it visits.
	 */
	private final class TypeFinder extends ExpressionVisitor {

		private final TypeJudgement dictType;
		private final TypeJudgement listType;
		private final TypeJudgement setType;
		private final TypeJudgement intType;
		private final TypeJudgement longType;
		private final TypeJudgement floatType;
		private final TypeJudgement strType;
		private final TypeJudgement tupleType;
		private final Top topType;
		private final SubgoalManager goalManager;

		public TypeFinder(Model model, Namespace scope,
				SubgoalManager goalManager) {
			this.goalManager = goalManager;
			dictType = new SetBasedTypeJudgement(new TClass(model.getTopLevel()
					.getClasses().get("dict")));
			listType = new SetBasedTypeJudgement(new TClass(model.getTopLevel()
					.getClasses().get("list")));
			setType = new SetBasedTypeJudgement(new TClass(model.getTopLevel()
					.getClasses().get("set")));
			intType = new SetBasedTypeJudgement(new TClass(model.getTopLevel()
					.getClasses().get("int")));
			longType = new SetBasedTypeJudgement(new TClass(model.getTopLevel()
					.getClasses().get("long")));
			floatType = new SetBasedTypeJudgement(new TClass(model
					.getTopLevel().getClasses().get("float")));
			strType = new SetBasedTypeJudgement(new TClass(model.getTopLevel()
					.getClasses().get("str")));
			tupleType = new SetBasedTypeJudgement(new TClass(model
					.getTopLevel().getClasses().get("tuple")));
			topType = new Top();
		}

		@Override
		public Object visitList(List node) throws Exception {
			return listType;
		}

		@Override
		public Object visitNum(Num node) throws Exception {
			switch (node.type) {
			case num_typeType.Int:
				return intType;
			case num_typeType.Long:
				return longType;
			case num_typeType.Float:
				return floatType;
			default:
				// TODO: Handle other numeric literal types
				return topType;
			}
		}

		@Override
		public Object visitStr(Str node) throws Exception {
			return strType;
		}

		@Override
		public Object visitAttribute(Attribute node) throws Exception {
			AttributeTypeGoal attributeTyper = new AttributeTypeGoal(model,
					scope, node);
			return goalManager.registerSubgoal(attributeTyper);
		}

		@Override
		public Object visitBinOp(BinOp node) throws Exception {
			return topType;
		}

		@Override
		public Object visitBoolOp(BoolOp node) throws Exception {
			return topType;
		}

		@Override
		public Object visitStarred(Starred node) throws Exception {
			return topType;
		}

		@Override
		public Object visitUnaryOp(UnaryOp node) throws Exception {
			return topType;
		}

		@Override
		public Object visitYield(Yield node) throws Exception {
			return topType;
		}

		@Override
		public Object visitCall(Call node) throws Exception {
			return topType;
		}

		@Override
		public Object visitIfExp(IfExp node) throws Exception {
			return topType;
		}

		@Override
		public Object visitCompare(Compare node) throws Exception {
			return topType;
		}

		@Override
		public Object visitComprehension(Comprehension node) throws Exception {
			return topType;
		}

		@Override
		public Object visitDict(Dict node) throws Exception {
			return dictType;
		}

		@Override
		public Object visitDictComp(DictComp node) throws Exception {
			return dictType;
		}

		@Override
		public Object visitExtSlice(ExtSlice node) throws Exception {
			return topType;
		}

		@Override
		public Object visitGeneratorExp(GeneratorExp node) throws Exception {
			return topType;
		}

		@Override
		public Object visitIndex(Index node) throws Exception {
			return topType;
		}

		@Override
		public Object visitLambda(Lambda node) throws Exception {
			return topType;
		}

		@Override
		public Object visitListComp(ListComp node) throws Exception {
			return listType;
		}

		@Override
		public Object visitName(Name node) throws Exception {
			NameTypeGoal typer = new NameTypeGoal(model, scope, node.id);
			return goalManager.registerSubgoal(typer);
		}

		@Override
		public Object visitNonLocal(NonLocal node) throws Exception {
			return topType;
		}

		@Override
		public Object visitRepr(Repr node) throws Exception {
			return strType;
		}

		@Override
		public Object visitSet(Set node) throws Exception {
			return setType;
		}

		@Override
		public Object visitSetComp(SetComp node) throws Exception {
			return setType;
		}

		@Override
		public Object visitSlice(Slice node) throws Exception {
			return topType;
		}

		@Override
		public Object visitStrJoin(StrJoin node) throws Exception {
			return topType;
		}

		@Override
		public Object visitSubscript(Subscript node) throws Exception {
			return topType;
		}

		@Override
		public Object visitTuple(Tuple node) throws Exception {
			return tupleType;
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			// TODO Auto-generated method stub
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {
			return null;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((expression == null) ? 0 : expression.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ExpressionTypeGoal))
			return false;
		ExpressionTypeGoal other = (ExpressionTypeGoal) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		return true;
	}
}
