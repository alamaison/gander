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
import uk.ac.ic.doc.gander.flowinference.result.Result.Processor;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.sendersgoals.FunctionSendersGoal;
import uk.ac.ic.doc.gander.flowinference.types.TCallable;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;

final class ParameterTypeGoal implements TypeGoal {

	private final InvokableCodeObject codeObject;
	private final String parameterName;

	ParameterTypeGoal(InvokableCodeObject codeObject, String parameterName) {

		if (!codeObject.formalParameters().hasParameterName(parameterName)) {
			throw new IllegalArgumentException("Parameter '" + parameterName
					+ "' doesn't appear in " + codeObject);
		}

		this.codeObject = codeObject;
		this.parameterName = parameterName;
	}

	public Result<Type> initialSolution() {
		return FiniteResult.bottom();
	}

	public Result<Type> recalculateSolution(SubgoalManager goalManager) {
		return new ParameterTypeGoalSolver(codeObject, parameterName,
				goalManager).solution();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((codeObject == null) ? 0 : codeObject.hashCode());
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
		ParameterTypeGoal other = (ParameterTypeGoal) obj;
		if (codeObject == null) {
			if (other.codeObject != null)
				return false;
		} else if (!codeObject.equals(other.codeObject))
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
		return "ParameterTypeGoal [codeObject=" + codeObject
				+ ", parameterName=" + parameterName + "]";
	}

}

final class ParameterTypeGoalSolver {

	private final Result<Type> solution;

	ParameterTypeGoalSolver(InvokableCodeObject codeObject,
			String parameterName, SubgoalManager goalManager) {

		Result<ModelSite<Call>> callSites = goalManager
				.registerSubgoal(new FunctionSendersGoal(codeObject));

		Concentrator<ModelSite<Call>, Type> processor = Concentrator
				.newInstance(new CallArgumentTyper(parameterName, codeObject,
						goalManager), TopT.INSTANCE);
		
		callSites.actOnResult(processor);

		solution = processor.result();
	}

	public Result<Type> solution() {
		return solution;
	}
}

final class CallArgumentTyper implements DatumProcessor<ModelSite<Call>, Type> {

	private final InvokableCodeObject codeObject;
	private final String parameterName;
	private final SubgoalManager goalManager;

	CallArgumentTyper(String parameterName, InvokableCodeObject codeObject,
			SubgoalManager goalManager) {
		this.parameterName = parameterName;
		this.codeObject = codeObject;
		this.goalManager = goalManager;
	}

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
				callSite, parameterName, codeObject, goalManager));
	}
};

final class CallsiteToParameterTypeMapper implements
		Transformer<Type, Result<Type>> {

	private final ModelSite<Call> callSite;
	private final String parameterName;
	private final InvokableCodeObject codeObject;
	private final SubgoalManager goalManager;

	CallsiteToParameterTypeMapper(ModelSite<Call> callSite,
			String parameterName, InvokableCodeObject codeObject,
			SubgoalManager goalManager) {
		this.callSite = callSite;
		this.parameterName = parameterName;
		this.codeObject = codeObject;
		this.goalManager = goalManager;
	}

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

	public Result<Type> transformFiniteResult(Set<Type> result) {
		RedundancyEliminator<Type> type = new RedundancyEliminator<Type>();

		for (Type typeAtCallSite : result) {

			if (typeAtCallSite instanceof TCallable) {

				TCallable callable = (TCallable) typeAtCallSite;

				if (callingObjectMightInvokeOurCodeObject(callable)) {
					type.add(((TCallable) typeAtCallSite)
							.typeOfArgumentAtNamedParameter(parameterName,
									callSite, goalManager));
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
						return result.contains(codeObject);
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
