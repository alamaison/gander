package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.List;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator.DatumProcessor;
import uk.ac.ic.doc.gander.flowinference.sendersgoals.FunctionSendersGoal;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.FunctionCO;

public class FunctionArgumentTypeGoal implements TypeGoal {

	private final FunctionCO function;
	private final String argument;

	public FunctionArgumentTypeGoal(FunctionCO function, String argument) {
		this.function = function;
		this.argument = argument;
	}

	public Result<Type> initialSolution() {
		return FiniteResult.bottom();
	}

	public Result<Type> recalculateSolution(SubgoalManager goalManager) {
		return new FunctionArgumentTypeGoalSolver(function, argument,
				goalManager).solution();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((argument == null) ? 0 : argument.hashCode());
		result = prime * result
				+ ((function == null) ? 0 : function.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FunctionArgumentTypeGoal other = (FunctionArgumentTypeGoal) obj;
		if (argument == null) {
			if (other.argument != null)
				return false;
		} else if (!argument.equals(other.argument))
			return false;
		if (function == null) {
			if (other.function != null)
				return false;
		} else if (!function.equals(other.function))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FunctionArgumentTypeGoal [function=" + function + ", argument="
				+ argument + "]";
	}

}

final class FunctionArgumentTypeGoalSolver {

	private final class ArgumentTyper implements
			DatumProcessor<ModelSite<Call>, Type> {

		public Result<Type> process(ModelSite<Call> callSite) {
			if (argumentIndex < callSite.astNode().args.length) {
				ModelSite<exprType> argument = new ModelSite<exprType>(callSite
						.astNode().args[argumentIndex], callSite.codeObject());

				return goalManager.registerSubgoal(new ExpressionTypeGoal(
						argument));
			} else {
				/*
				 * Few argument were passed to the function than are declared in
				 * its signature. It's probably expecting default arguments.
				 */
				if (argumentIndex < function.ast().args.defaults.length) {
					exprType defaultVal = function.ast().args.defaults[argumentIndex];
					if (defaultVal != null) {

						ModelSite<exprType> defaultArgument = new ModelSite<exprType>(
								defaultVal, function.parent());
						/*
						 * XXX: Are we sure default arguments are evaluated in
						 * the context of the function's parent?
						 */
						return goalManager
								.registerSubgoal(new ExpressionTypeGoal(
										defaultArgument));
					} else {
						/* No default. The program is probably wrong. */
						return TopT.INSTANCE;
					}
				} else {
					/*
					 * No idea what's going on here. The defaults array seems to
					 * be smaller than the argument array
					 */
					assert false;
					return TopT.INSTANCE;
				}
			}
		}
	}

	private final int argumentIndex;
	private final FunctionCO function;
	private final SubgoalManager goalManager;
	private final Result<Type> solution;

	public FunctionArgumentTypeGoalSolver(FunctionCO function, String argument,
			SubgoalManager goalManager) {
		this.function = function;
		this.goalManager = goalManager;
		this.argumentIndex = findArgumentIndexInFunction(function, argument);

		Result<ModelSite<Call>> callSites = goalManager
				.registerSubgoal(new FunctionSendersGoal(function));

		Concentrator<ModelSite<Call>, Type> processor = Concentrator
				.newInstance(new ArgumentTyper(), TopT.INSTANCE);
		callSites.actOnResult(processor);
		solution = processor.result();
	}

	public Result<Type> solution() {
		return solution;
	}

	private static int findArgumentIndexInFunction(FunctionCO function,
			String argument) {
		List<ModelSite<exprType>> args = function.codeBlock()
				.getFormalParameters();

		for (int i = 0; i < args.size(); ++i) {
			exprType arg = args.get(i).astNode();
			if (arg instanceof Name && ((Name) arg).id.equals(argument))
				return i;
		}

		return -1;
	}

}
