package uk.ac.ic.doc.gander.flowinference.typegoals;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator.DatumProcessor;
import uk.ac.ic.doc.gander.flowinference.sendersgoals.FunctionSendersGoal;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.FunctionCO;

public class FunctionParameterTypeGoal implements TypeGoal {

	private final FunctionCO function;
	private final String argument;

	public FunctionParameterTypeGoal(FunctionCO function, String argument) {
		this.function = function;
		this.argument = argument;
	}

	public Result<Type> initialSolution() {
		return FiniteResult.bottom();
	}

	public Result<Type> recalculateSolution(SubgoalManager goalManager) {
		return new FunctionParameterTypeGoalSolver(function, argument,
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
		FunctionParameterTypeGoal other = (FunctionParameterTypeGoal) obj;
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
		return "FunctionParameterTypeGoal [function=" + function
				+ ", argument=" + argument + "]";
	}

}

final class FunctionParameterTypeGoalSolver {

	private final class ArgumentTypeConcentrator implements
			DatumProcessor<ModelSite<Call>, Type> {

		private final String parameterName;

		public ArgumentTypeConcentrator(String parameterName) {
			this.parameterName = parameterName;
		}

		/**
		 * Given a set of arguments passed to a call-site known to call a
		 * particular function, finds the type of argument that is passed to the
		 * named parameter of the function.
		 * 
		 * Part of the job is finding which argument at the call-site is
		 * actually passed to the named parameter. This may not be a simple
		 * matter of order.
		 */
		public Result<Type> process(ModelSite<Call> callSite) {
			// XXX: We are asserting that this is of function type. Infer?
			return new TFunction(function).typeOfArgumentAtNamedParameter(
					parameterName, callSite, goalManager);
		}
	}

	private final FunctionCO function;
	private final SubgoalManager goalManager;
	private final Result<Type> solution;

	public FunctionParameterTypeGoalSolver(FunctionCO function,
			String parameterName, SubgoalManager goalManager) {
		this.function = function;
		this.goalManager = goalManager;

		Result<ModelSite<Call>> callSites = goalManager
				.registerSubgoal(new FunctionSendersGoal(function));

		Concentrator<ModelSite<Call>, Type> processor = Concentrator
				.newInstance(new ArgumentTypeConcentrator(parameterName),
						TopT.INSTANCE);
		callSites.actOnResult(processor);
		solution = processor.result();
	}

	public Result<Type> solution() {
		return solution;
	}

}
