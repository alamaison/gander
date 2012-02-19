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
import uk.ac.ic.doc.gander.model.parameters.FormalParameter;

final class ParameterTypeGoal implements TypeGoal {

	private final FormalParameter parameter;

	ParameterTypeGoal(FormalParameter parameter) {
		assert parameter != null;
		this.parameter = parameter;
	}

	@Override
	public Result<Type> initialSolution() {
		return FiniteResult.bottom();
	}

	@Override
	public Result<Type> recalculateSolution(SubgoalManager goalManager) {
		return new ParameterTypeGoalSolver(parameter, goalManager).solution();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((parameter == null) ? 0 : parameter.hashCode());
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
		ParameterTypeGoal other = (ParameterTypeGoal) obj;
		if (parameter == null) {
			if (other.parameter != null)
				return false;
		} else if (!parameter.equals(other.parameter))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ParameterTypeGoal [parameter=" + parameter + "]";
	}

}

final class ParameterTypeGoalSolver {

	private final Result<Type> solution;

	ParameterTypeGoalSolver(FormalParameter parameter,
			SubgoalManager goalManager) {

		Result<ModelSite<Call>> callSites = goalManager
				.registerSubgoal(new FunctionSendersGoal(
						(InvokableCodeObject) parameter.site().codeObject()));

		Concentrator<ModelSite<Call>, Type> processor = Concentrator
				.newInstance(new CallArgumentTyper(parameter, goalManager),
						TopT.INSTANCE);

		callSites.actOnResult(processor);

		solution = processor.result();
	}

	public Result<Type> solution() {
		return solution;
	}
}

final class CallArgumentTyper implements DatumProcessor<ModelSite<Call>, Type> {

	private final FormalParameter parameter;
	private final SubgoalManager goalManager;

	CallArgumentTyper(FormalParameter parameter, SubgoalManager goalManager) {
		this.parameter = parameter;
		this.goalManager = goalManager;
	}

	@Override
	public Result<Type> process(final ModelSite<Call> callSite) {

		/*
		 * Which argument is passed to the parameter depends on how this code
		 * object ends up being invoked by the call site. This a function of the
		 * type of object being called at the call site (which might not be this
		 * code object).
		 * 
		 * If the callable is a bound method, the arguments are passed to
		 * different parameters than if it is used unbound. This determination
		 * is made by the type object of the callable at the callsite so we need
		 * to infer this type first.
		 */
		Result<Type> callableType = goalManager
				.registerSubgoal(new ExpressionTypeGoal(
						new ModelSite<exprType>(callSite.astNode().func,
								callSite.codeObject())));

		/*
		 * But now that we've got a set of types for the callsite, we don't want
		 * to include all of them; only those that are implemented by our code
		 * object.
		 */
		return callableType.transformResult(new CallsiteToParameterTypeMapper(
				callSite, parameter, goalManager));
	}
};

final class CallsiteToParameterTypeMapper implements
		Transformer<Type, Result<Type>> {

	private final ModelSite<Call> callSite;
	private final FormalParameter parameter;
	private final SubgoalManager goalManager;

	CallsiteToParameterTypeMapper(ModelSite<Call> callSite,
			FormalParameter parameter, SubgoalManager goalManager) {
		this.callSite = callSite;
		this.parameter = parameter;
		this.goalManager = goalManager;
	}

	@Override
	public Result<Type> transformInfiniteResult() {
		/*
		 * No idea how we are being called so don't know what our parameter is
		 */
		return TopT.INSTANCE;
		/*
		 * TODO: could take the union of the types of all the arguments passed
		 * at the callsite as it is only which of them that gets passed that we
		 * don't know
		 */
	}

	@Override
	public Result<Type> transformFiniteResult(Set<Type> result) {
		RedundancyEliminator<Type> type = new RedundancyEliminator<Type>();

		for (Type typeAtCallSite : result) {

			if (typeAtCallSite instanceof TCallable) {

				TCallable callable = (TCallable) typeAtCallSite;

				if (callingObjectMightInvokeOurCodeObject(callable)) {
					type.add(((TCallable) typeAtCallSite)
							.typeOfArgumentPassedToParameter(parameter,
									callSite, goalManager));
					if (type.isFinished()) {
						break;
					}
				}

			} else {
				System.err.println("WTF: call site isn't calling a callable: "
						+ typeAtCallSite);
			}
		}

		return type.result();
	}

	public boolean callingObjectMightInvokeOurCodeObject(TCallable object) {

		return object.codeObjectsInvokedByCall(goalManager).transformResult(
				new Transformer<InvokableCodeObject, Boolean>() {

					@Override
					public Boolean transformFiniteResult(
							Set<InvokableCodeObject> result) {

						return result.contains(parameter.site().codeObject());
					}

					@Override
					public Boolean transformInfiniteResult() {
						/*
						 * We can't tell if this possible receiver of the call
						 * in question is implemented by the code object whose
						 * parameter we are looking at.
						 */
						return false;
					}
				});
	}
}
