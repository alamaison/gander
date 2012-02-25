package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
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
import uk.ac.ic.doc.gander.model.name_binding.Variable;
import uk.ac.ic.doc.gander.model.parameters.FormalParameter;

/**
 * Find the types that the given parameter may bind to the given variable name.
 * 
 * In Python, a single parameter may bind more than one name which is why the
 * query is framed in this way.
 */
final class ParameterTypeGoal implements TypeGoal {

	private final InvokableCodeObject invokable;
	private final Variable variable;

	ParameterTypeGoal(InvokableCodeObject invokable, Variable variable) {
		assert variable != null;
		assert invokable.formalParameters().hasVariableBindingParameter(
				variable);

		this.invokable = invokable;
		this.variable = variable;
	}

	@Override
	public Result<Type> initialSolution() {
		return FiniteResult.bottom();
	}

	@Override
	public Result<Type> recalculateSolution(SubgoalManager goalManager) {
		if (goalManager == null)
			throw new NullPointerException("Goal manager required");

		return new ParameterTypeGoalSolver(invokable, variable, goalManager)
				.solution();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((invokable == null) ? 0 : invokable.hashCode());
		result = prime * result
				+ ((variable == null) ? 0 : variable.hashCode());
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
		if (invokable == null) {
			if (other.invokable != null)
				return false;
		} else if (!invokable.equals(other.invokable))
			return false;
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.equals(other.variable))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ParameterTypeGoal [invokable=" + invokable + ", variable="
				+ variable + "]";
	}

}

final class ParameterTypeGoalSolver {

	private final Result<Type> solution;
	private final SubgoalManager goalManager;
	private final Variable variable;

	ParameterTypeGoalSolver(InvokableCodeObject invokable, Variable variable,
			SubgoalManager goalManager) {
		assert variable != null;
		assert invokable.formalParameters().hasVariableBindingParameter(
				variable);
		assert goalManager != null;

		this.goalManager = goalManager;
		this.variable = variable;

		Result<ModelSite<Call>> callSites = goalManager
				.registerSubgoal(new FunctionSendersGoal(invokable));

		solution = deriveParameterTypeFromCallsites(invokable, callSites);
	}

	private Result<Type> deriveParameterTypeFromCallsites(
			InvokableCodeObject invokable, Result<ModelSite<Call>> callSites) {

		Concentrator<ModelSite<Call>, Type> processor = Concentrator
				.newInstance(new CallArgumentTyper(invokable, variable,
						goalManager), TopT.INSTANCE);

		callSites.actOnResult(processor);

		return processor.result();
	}

	public Result<Type> solution() {
		return solution;
	}
}

/**
 * Transforms a call-site into the types that may be bound to the given
 * variable.
 */
final class CallArgumentTyper implements DatumProcessor<ModelSite<Call>, Type> {

	private final InvokableCodeObject invokable;
	private final SubgoalManager goalManager;
	private final Variable variable;

	CallArgumentTyper(InvokableCodeObject invokable, Variable variable,
			SubgoalManager goalManager) {
		assert variable != null;
		assert invokable.formalParameters().hasVariableBindingParameter(
				variable);
		assert goalManager != null;

		this.invokable = invokable;
		this.variable = variable;
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
		return callableType.transformResult(new CallsiteToVariableTypeMapper(
				callSite, variable, invokable, goalManager));
	}
};

final class CallsiteToVariableTypeMapper implements
		Transformer<Type, Result<Type>> {

	private final ModelSite<Call> callSite;
	private final InvokableCodeObject invokable;
	private final SubgoalManager goalManager;
	private final Variable variable;

	CallsiteToVariableTypeMapper(ModelSite<Call> callSite, Variable variable,
			InvokableCodeObject invokable, SubgoalManager goalManager) {
		assert variable != null;
		assert invokable.formalParameters().hasVariableBindingParameter(
				variable);
		assert goalManager != null;

		this.callSite = callSite;
		this.variable = variable;
		this.invokable = invokable;
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

				type.add(typeContributedByCallingCallable((TCallable) typeAtCallSite));

			} else {
				System.err.println("UNTYPABLE: call site isn't "
						+ "calling a callable: " + typeAtCallSite);
			}

			if (type.isFinished()) {
				break;
			}
		}

		return type.result();
	}

	private Result<Type> typeContributedByCallingCallable(TCallable callable) {

		if (callingObjectMightInvokeOurCodeObject(callable)) {

			FormalParameter parameter = invokable.formalParameters()
					.variableBindingParameter(variable);

			Result<Argument> arguments = callable.argumentsPassedToParameter(
					parameter, callSite, goalManager);

			return arguments
					.transformResult(new Transformer<Argument, Result<Type>>() {

						@Override
						public Result<Type> transformFiniteResult(
								Set<Argument> arguments) {

							RedundancyEliminator<Type> parameterType = new RedundancyEliminator<Type>();

							for (Argument argument : arguments) {
								parameterType.add(argument.type(goalManager));

								if (parameterType.isFinished())
									break;
							}

							return parameterType.result();
						}

						@Override
						public Result<Type> transformInfiniteResult() {
							return TopT.INSTANCE;
						}
					});

		} else {
			/*
			 * calling this callable never results in our code object being
			 * invoked so it doesn't contribute to the type of the parameter's
			 * bindings.
			 */
			return FiniteResult.bottom();
		}
	}

	public boolean callingObjectMightInvokeOurCodeObject(TCallable object) {

		return object.codeObjectsInvokedByCall(goalManager).transformResult(
				new Transformer<InvokableCodeObject, Boolean>() {

					@Override
					public Boolean transformFiniteResult(
							Set<InvokableCodeObject> codeObjectsInvokedByCall) {

						return codeObjectsInvokedByCall.contains(invokable);
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
