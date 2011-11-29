package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Collections;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator.DatumProcessor;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TObject;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;

public final class ReturnTypeGoal implements TypeGoal {

	private final ModelSite<Call> callSite;

	public ReturnTypeGoal(ModelSite<Call> callSite) {
		this.callSite = callSite;
	}

	public Result<Type> initialSolution() {
		return FiniteResult.bottom();
	}

	public Result<Type> recalculateSolution(final SubgoalManager goalManager) {
		ModelSite<exprType> callable = new ModelSite<exprType>(callSite
				.astNode().func, callSite.codeObject());

		ExpressionTypeGoal callableTyper = new ExpressionTypeGoal(callable);
		Result<Type> callableTypes = goalManager.registerSubgoal(callableTyper);

		Concentrator<Type, Type> action = Concentrator.newInstance(
				new DatumProcessor<Type, Type>() {

					public Result<Type> process(Type callableType) {
						if (callableType instanceof TClass) {
							/*
							 * Calling a class is a constructor call.
							 * Constructors are special functions so we can
							 * infer the return type immediately. It is an
							 * instance of the class being called.
							 */
							return new FiniteResult<Type>(Collections
							.singleton(new TObject(
									((TClass) callableType)
											.getClassInstance())));
						} else if (callableType instanceof TFunction) {
							FunctionReturnTypeGoal typer = new FunctionReturnTypeGoal(
									((TFunction) callableType)
											.getFunctionInstance());
							return goalManager.registerSubgoal(typer);
						} else {
							return TopT.INSTANCE;
						}
					}
				}, TopT.INSTANCE);
		callableTypes.actOnResult(action);

		return action.result();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((callSite == null) ? 0 : callSite.hashCode());
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
		ReturnTypeGoal other = (ReturnTypeGoal) obj;
		if (callSite == null) {
			if (other.callSite != null)
				return false;
		} else if (!callSite.equals(other.callSite))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ReturnTypeGoal [callSite=" + callSite + "]";
	}

}
