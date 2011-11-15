package uk.ac.ic.doc.gander.flowinference.typegoals;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Return;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ast.LocalCodeBlockVisitor;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeConcentrator;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelSite;

public class FunctionReturnTypeGoal implements TypeGoal {

	private final Model model;
	private final Function function;

	public FunctionReturnTypeGoal(Model model, Function function) {
		this.model = model;
		this.function = function;
	}

	public TypeJudgement initialSolution() {
		return SetBasedTypeJudgement.BOTTOM;
	}

	public TypeJudgement recalculateSolution(final SubgoalManager goalManager) {
		final TypeConcentrator returnTypes = new TypeConcentrator();

		/* Bizarre declaration to allow modification in anonymous class */
		final boolean seenReturnStatement[] = { false };
		try {
			function.asCodeBlock().accept(new LocalCodeBlockVisitor() {

				@Override
				public Object visitReturn(Return node) throws Exception {
					if (returnTypes.isFinished())
						return null;

					seenReturnStatement[0] = true;

					if (node.value != null) {
						ModelSite<exprType> returnValue = new ModelSite<exprType>(
								node.value, function, model);
						ExpressionTypeGoal typer = new ExpressionTypeGoal(
								returnValue);
						returnTypes.add(goalManager.registerSubgoal(typer));
					} else {
						/*
						 * A bare 'return' statement means that the function
						 * returns builtin None.
						 */
						NameTypeGoal typer = new NameTypeGoal(model, function,
								"None");
						returnTypes.add(goalManager.registerSubgoal(typer));
					}
					return null;
				}

				@Override
				protected Object unhandled_node(SimpleNode node)
						throws Exception {
					return null;
				}

				@Override
				public void traverse(SimpleNode node) throws Exception {
					// want all 'return' statements in code block
					if (!returnTypes.isFinished())
						node.traverse(this);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (!returnTypes.isFinished() && !seenReturnStatement[0]) {
			/*
			 * A missing 'return' statement means that the function returns
			 * builtin None.
			 */
			NameTypeGoal typer = new NameTypeGoal(model, function, "None");
			returnTypes.add(goalManager.registerSubgoal(typer));
		}

		return returnTypes.getJudgement();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((function == null) ? 0 : function.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FunctionReturnTypeGoal))
			return false;
		FunctionReturnTypeGoal other = (FunctionReturnTypeGoal) obj;
		if (function == null) {
			if (other.function != null)
				return false;
		} else if (!function.equals(other.function))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FunctionReturnTypeGoal [function=" + function + "]";
	}

}
