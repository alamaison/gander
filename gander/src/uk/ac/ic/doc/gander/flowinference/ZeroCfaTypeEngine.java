package uk.ac.ic.doc.gander.flowinference;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.Goal;
import uk.ac.ic.doc.gander.flowinference.dda.GoalSolver;
import uk.ac.ic.doc.gander.flowinference.dda.KnowledgeBase;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

interface TypeEngine {

	/**
	 * Infer the type of the expression.
	 */
	public Result<Type> typeOf(ModelSite<? extends exprType> expression);

	@Deprecated
	public Result<Type> typeOf(exprType expression, CodeObject scope);

}

/**
 * Flow-insensitive, context-insensitive, container-insensitive type inference
 * engine.
 */
public final class ZeroCfaTypeEngine implements TypeEngine {
	private KnowledgeBase blackboard = new KnowledgeBase();

	public ZeroCfaTypeEngine() {
	}

	public Result<Type> typeOf(ModelSite<? extends exprType> expression) {
		Goal<Result<Type>> rootGoal = new ExpressionTypeGoal(expression);
		System.out.print("Inferring type of " + expression);
		GoalSolver<Result<Type>> solver = GoalSolver.newInstance(rootGoal,
				blackboard);
		Result<Type> j = solver.solve();
		System.out.println(" as " + j);
		return j;
	}

	public Result<Type> typeOf(exprType expression, CodeObject scope) {
		return typeOf(new ModelSite<exprType>(expression, scope));
	}
}
