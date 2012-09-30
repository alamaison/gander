package uk.ac.ic.doc.gander.flowinference;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.Goal;
import uk.ac.ic.doc.gander.flowinference.dda.GoalSolver;
import uk.ac.ic.doc.gander.flowinference.dda.KnowledgeBase;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.expression.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.typegoals.variable.VariableTypeGoal;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

/**
 * Flow-insensitive, context-insensitive, container-insensitive type inference
 * engine.
 */
public final class ZeroCfaTypeEngine implements TypeEngine {
	private final KnowledgeBase blackboard = new KnowledgeBase();

	public ZeroCfaTypeEngine() {
	}

	@Override
	public Result<Type> typeOf(ModelSite<? extends exprType> expression) {
		Goal<Result<Type>> rootGoal = new ExpressionTypeGoal(expression);
		// System.out.print("Inferring type of " + expression);
		GoalSolver<Result<Type>> solver = GoalSolver.newInstance(rootGoal,
				blackboard);
		Result<Type> j = solver.solve();
		// System.out.println(" as " + j);
		return j;
	}

	public Result<Type> typeOf(Variable variable) {
		Goal<Result<Type>> rootGoal = new VariableTypeGoal(variable);
		// System.out.print("Inferring type of " + variable);
		GoalSolver<Result<Type>> solver = GoalSolver.newInstance(rootGoal,
				blackboard);
		Result<Type> j = solver.solve();
		// System.out.println(" as " + j);
		return j;
	}

	@Override
	public Result<Type> typeOf(exprType expression, CodeObject scope) {
		return typeOf(new ModelSite<exprType>(expression, scope));
	}
}
