package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator.DatumProcessor;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.sendersgoals.FunctionSendersGoal;
import uk.ac.ic.doc.gander.flowinference.types.TCallable;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;
import uk.ac.ic.doc.gander.model.codeobject.FunctionCO;

final class MethodParameterTypeGoal implements TypeGoal {

	private final FunctionCO method;
	private final String name;

	MethodParameterTypeGoal(FunctionCO method, String name) {
		assert method.parent() instanceof ClassCO;
		this.method = method;
		this.name = name;
	}

	public Result<Type> initialSolution() {
		return FiniteResult.bottom();
	}

	public Result<Type> recalculateSolution(SubgoalManager goalManager) {
		return new MethodParameterTypeGoalSolver(method, name, goalManager)
				.solution();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		MethodParameterTypeGoal other = (MethodParameterTypeGoal) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MethodParameterTypeGoal [method=" + method + ", parameterName="
				+ name + "]";
	}

}

final class MethodParameterTypeGoalSolver {

	private final class CallArgumentTyper implements
			DatumProcessor<ModelSite<Call>, Type> {

		public Result<Type> process(final ModelSite<Call> callSite) {

			/*
			 * Which argument is passed to the parameter depends on whether this
			 * callsite calls the method implicitly on an object instance or
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

	MethodParameterTypeGoalSolver(FunctionCO method, String parameterName,
			SubgoalManager goalManager) {
		assert method.parent() instanceof ClassCO;

		this.parameterName = parameterName;
		this.goalManager = goalManager;

		RedundancyEliminator<ModelSite<Call>> callSites = new RedundancyEliminator<ModelSite<Call>>();
		if (method.declaredName().equals("__init__")) {
			callSites.add(goalManager.registerSubgoal(new FunctionSendersGoal(
					(ClassCO) method.parent())));
		}

		callSites.add(goalManager.registerSubgoal(new FunctionSendersGoal(
				method)));

		Concentrator<ModelSite<Call>, Type> processor = Concentrator
				.newInstance(new CallArgumentTyper(), TopT.INSTANCE);
		callSites.result().actOnResult(processor);

		solution = processor.result();
	}

	public Result<Type> solution() {
		return solution;
	}

}
