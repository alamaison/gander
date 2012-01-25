package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator.DatumProcessor;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.sendersgoals.FunctionSendersGoal;
import uk.ac.ic.doc.gander.flowinference.types.TCallable;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;

final class CallableParameterTypeGoal implements TypeGoal {

	private final InvokableCodeObject callable;
	private final String parameterName;

	CallableParameterTypeGoal(InvokableCodeObject callable, String parameterName) {
		if (callable.formalParameters().namedParameter(parameterName) == null) {
			throw new IllegalArgumentException("Parameter '" + parameterName
					+ "' doesn't appear in " + callable);
		}

		this.callable = callable;
		this.parameterName = parameterName;
	}

	public Result<Type> initialSolution() {
		return FiniteResult.bottom();
	}

	public Result<Type> recalculateSolution(SubgoalManager goalManager) {
		return new CallableParameterTypeGoalSolver(callable, parameterName,
				goalManager).solution();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((callable == null) ? 0 : callable.hashCode());
		result = prime * result
				+ ((parameterName == null) ? 0 : parameterName.hashCode());
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
		CallableParameterTypeGoal other = (CallableParameterTypeGoal) obj;
		if (callable == null) {
			if (other.callable != null)
				return false;
		} else if (!callable.equals(other.callable))
			return false;
		if (parameterName == null) {
			if (other.parameterName != null)
				return false;
		} else if (!parameterName.equals(other.parameterName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CallableParameterTypeGoal [callable=" + callable
				+ ", parameterName=" + parameterName + "]";
	}

}

final class CallableParameterTypeGoalSolver {

	private final class CallArgumentTyper implements
			DatumProcessor<ModelSite<Call>, Type> {

		public Result<Type> process(final ModelSite<Call> callSite) {

			/*
			 * Which argument is passed to the parameter depends on whether this
			 * callsite calls the callable implicitly on an object instance or
			 * explictly on a class, passing the object instance as the first
			 * argument.
			 */
			Result<Type> callableType = goalManager
					.registerSubgoal(new ExpressionTypeGoal(
							new ModelSite<exprType>(callSite.astNode().func,
									callSite.codeObject())));
			return callableType
					.transformResult(new Transformer<Type, Result<Type>>() {

						public Result<Type> transformInfiniteResult() {
							/*
							 * No idea how we are being called so don't know
							 * what our parameter is
							 */
							return TopT.INSTANCE;
							/*
							 * TODO: could take the union of the types of all
							 * the arguments passed at the callsite as it is
							 * only which of them that gets passed that we don't
							 * know
							 */
						}

						public Result<Type> transformFiniteResult(
								Set<Type> result) {
							RedundancyEliminator<Type> type = new RedundancyEliminator<Type>();

							for (Type callableType : result) {

								if (callableType instanceof TCallable) {

									type.add(((TCallable) callableType)
											.typeOfArgumentAtNamedParameter(
													parameterName, callSite,
													goalManager));

								} else {
									System.err
											.println("WTF: call site isn't calling a callable: "
													+ callableType);
								}
							}

							return type.result();
						}
					});
		}
	};

	private final SubgoalManager goalManager;
	private final Result<Type> solution;
	private final String parameterName;

	CallableParameterTypeGoalSolver(InvokableCodeObject callable,
			String parameterName, SubgoalManager goalManager) {

		this.parameterName = parameterName;
		this.goalManager = goalManager;

		Result<ModelSite<Call>> callSites = goalManager
				.registerSubgoal(new FunctionSendersGoal(callable));

		Concentrator<ModelSite<Call>, Type> processor = Concentrator
				.newInstance(new CallArgumentTyper(), TopT.INSTANCE);
		callSites.actOnResult(processor);

		solution = processor.result();
	}

	public Result<Type> solution() {
		return solution;
	}

}
