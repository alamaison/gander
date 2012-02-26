package uk.ac.ic.doc.gander.flowinference.typegoals.returns;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator.DatumProcessor;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.flowinference.typegoals.expression.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.types.TCallable;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;

final class ReturnTypeGoalSolver {

	private final SubgoalManager goalManager;
	private final Result<Type> solution;

	public ReturnTypeGoalSolver(SubgoalManager goalManager,
			ModelSite<Call> callSite) {
		this.goalManager = goalManager;

		ModelSite<exprType> callable = new ModelSite<exprType>(
				callSite.astNode().func, callSite.codeObject());

		ExpressionTypeGoal callableTyper = new ExpressionTypeGoal(callable);
		Result<Type> callableTypes = goalManager.registerSubgoal(callableTyper);

		Concentrator<Type, Type> action = Concentrator.newInstance(
				new ReturnTypeProcessor(), TopT.INSTANCE);
		callableTypes.actOnResult(action);

		solution = action.result();
	}

	private class ReturnTypeProcessor implements DatumProcessor<Type, Type> {

		@Override
		public Result<Type> process(Type callableType) {
			if (callableType instanceof TCallable) {
				return ((TCallable) callableType).returnType(goalManager);
			} else {
				return TopT.INSTANCE;
			}
		}
	}

	Result<Type> solution() {
		return solution;
	}

}