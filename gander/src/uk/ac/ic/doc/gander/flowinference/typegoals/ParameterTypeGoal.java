package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.call.DefaultCall;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator.DatumProcessor;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.result.Top;
import uk.ac.ic.doc.gander.flowinference.sendersgoals.FunctionSendersGoal;
import uk.ac.ic.doc.gander.flowinference.types.TCallable;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

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

	private final InvokableCodeObject invokable;
	private final Variable variable;
	private final SubgoalManager goalManager;
	private final Result<Type> solution;

	ParameterTypeGoalSolver(InvokableCodeObject invokable, Variable variable,
			SubgoalManager goalManager) {
		assert variable != null;
		assert invokable.formalParameters().hasVariableBindingParameter(
				variable);
		assert goalManager != null;

		this.invokable = invokable;
		this.goalManager = goalManager;
		this.variable = variable;

		Result<ModelSite<Call>> callSites = goalManager
				.registerSubgoal(new FunctionSendersGoal(invokable));

		solution = deriveParameterTypeFromCallsites(callSites);
	}

	private Result<Type> processSender(ModelSite<Call> senderCallSite) {

		/*
		 * The FunctionSendersGoal has told us which expressions it thinks our
		 * parameter's code object has flown to but that's only half the story.
		 * It hasn't told us what callable object is flows there in (yes, we
		 * should probably change the flow goals to better model this but, for
		 * the moment, that's how it is) so we have to use type inference to get
		 * the types as these affect the way arguments are passed to this code
		 * object.
		 * 
		 * TODO: deal with this by tracking the type using the flow goal model
		 */

		Result<Type> callSiteTypes = inferObjectsCalledAtCallSite(senderCallSite);

		/*
		 * But now there may be all sorts of other objects in here that don't
		 * invoke our code object when they are called. For example, if the
		 * callsite that we tracked our code object's flow to is polymorphic and
		 * also invokes other code objects.
		 * 
		 * We filter those out now.
		 */
		Result<TCallable> callables = filterOutUnrelatedObjects(callSiteTypes);

		/*
		 * Finally we can get on to inferring the type of the parameter. This
		 * involved a complex little dance between the arguments at the
		 * callsite, the type of object being called and the parameters of the
		 * code object. They each contribute something to the negotiation that
		 * leads to values flowing from the call-site to variables in the code
		 * object. The code object could be called multiple ways at the same
		 * call-site and each call affects the parameter type differently so the
		 * code object receiver and the object it is being called as part of are
		 * packaged up along with the call-site itself (for its arguments) as a
		 * Call object that takes care of coordinating the dance.
		 */
		Result<uk.ac.ic.doc.gander.flowinference.call.Call> calls = packageCalls(
				callables, senderCallSite);

		/*
		 * Dance.
		 */
		return deriveParameterTypeFromCalls(calls);
	}

	/**
	 * Transforms a call-site into the objects that are called at it.
	 * 
	 * Which argument is passed to a parameter depends on how its code object is
	 * invoked by the call site. For example, if the callable is a bound method,
	 * the arguments are passed to different parameters than if it is used
	 * unbound. This is decided by {@link Type} being called at the call site
	 * (which might not call the code object in question).
	 */
	private Result<Type> inferObjectsCalledAtCallSite(ModelSite<Call> callSite) {

		return goalManager.registerSubgoal(new ExpressionTypeGoal(
				new ModelSite<exprType>(callSite.astNode().func, callSite
						.codeObject())));
	}

	private Result<TCallable> filterOutUnrelatedObjects(
			Result<Type> callSiteTypes) {

		return callSiteTypes.transformResult(new CalledObjectFilter(invokable,
				goalManager));
	}

	private Result<uk.ac.ic.doc.gander.flowinference.call.Call> packageCalls(
			Result<TCallable> callables, final ModelSite<Call> senderCallSite) {

		return callables.transformResult(new CallPackager(invokable,
				senderCallSite));
	}

	private Result<Type> deriveParameterTypeFromCalls(
			Result<uk.ac.ic.doc.gander.flowinference.call.Call> calls) {

		return calls.transformResult(new CallDancer(variable, goalManager));
	}

	private Result<Type> deriveParameterTypeFromCallsites(
			Result<ModelSite<Call>> callSites) {

		Concentrator<ModelSite<Call>, Type> processor = Concentrator
				.newInstance(new DatumProcessor<ModelSite<Call>, Type>() {

					@Override
					public Result<Type> process(ModelSite<Call> senderCallSite) {
						return processSender(senderCallSite);
					}
				}, TopT.INSTANCE);

		callSites.actOnResult(processor);
		return processor.result();
	}

	public Result<Type> solution() {
		return solution;
	}
}

/**
 * Transforms a call-site into the objects that are called at it.
 * 
 * Which argument is passed to a parameter depends on how its code object is
 * invoked by the call site. For example, if the callable is a bound method, the
 * arguments are passed to different parameters than if it is used unbound. This
 * is decided by {@link Type} being called at the call site (which might not
 * call the code object in question).
 */
final class CalledObjectTyper implements DatumProcessor<ModelSite<Call>, Type> {

	private final SubgoalManager goalManager;

	CalledObjectTyper(SubgoalManager goalManager) {
		assert goalManager != null;
		this.goalManager = goalManager;
	}

	@Override
	public Result<Type> process(ModelSite<Call> callSite) {

		return goalManager.registerSubgoal(new ExpressionTypeGoal(
				new ModelSite<exprType>(callSite.astNode().func, callSite
						.codeObject())));
	}
};

/**
 * Transforms a call-site into the types that may be bound to the given
 * variable.
 */
/*
 * But now that we've got a set of types for the callsite, we don't want to
 * include all of them; only those that are implemented by our code object.
 */
final class CalledObjectFilter implements Transformer<Type, Result<TCallable>> {

	private final InvokableCodeObject invokable;
	private final SubgoalManager goalManager;

	CalledObjectFilter(InvokableCodeObject invokable, SubgoalManager goalManager) {
		assert invokable != null;
		assert goalManager != null;

		this.invokable = invokable;
		this.goalManager = goalManager;
	}

	private class TopCallable extends Top<TCallable> {

		@Override
		public String toString() {
			return "⊤callable";
		}
	}

	@Override
	public Result<TCallable> transformFiniteResult(Set<Type> objectsAtCallSite) {

		Set<TCallable> contributingCallables = new HashSet<TCallable>();

		for (Type calledObject : objectsAtCallSite) {

			if (calledObject instanceof TCallable) {

				TCallable callable = (TCallable) calledObject;

				switch (callingObjectMightInvokeOurCodeObject(callable)) {
				case YES:
					contributingCallables.add(callable);
					break;

				case NO:
					/*
					 * calling this callable never results in our code object
					 * being invoked so it doesn't contribute to the type of the
					 * parameter's bindings.
					 */
					break;

				case NOT_A_CLUE:
					return new TopCallable();

				default:
					throw new AssertionError();
				}

			} else {
				System.err.println("UNTYPABLE: call site isn't "
						+ "calling a callable: " + calledObject);
			}
		}

		return new FiniteResult<TCallable>(contributingCallables);
	}

	@Override
	public Result<TCallable> transformInfiniteResult() {
		/*
		 * No idea how we are being called so don't know what our parameter is
		 */
		return new TopCallable();
		/*
		 * TODO: could take the union of the types of all the arguments passed
		 * at the callsite as it is only which of them that gets passed that we
		 * don't know
		 */
	}

	private enum ObjectIsRelevant {
		YES, NO, NOT_A_CLUE
	}

	private ObjectIsRelevant callingObjectMightInvokeOurCodeObject(
			TCallable object) {

		return object.codeObjectsInvokedByCall(goalManager).transformResult(
				new Transformer<InvokableCodeObject, ObjectIsRelevant>() {

					@Override
					public ObjectIsRelevant transformFiniteResult(
							Set<InvokableCodeObject> codeObjectsInvokedByCall) {

						if (codeObjectsInvokedByCall.contains(invokable)) {
							return ObjectIsRelevant.YES;
						} else {
							return ObjectIsRelevant.NO;
						}
					}

					@Override
					public ObjectIsRelevant transformInfiniteResult() {
						/*
						 * We can't tell if this possible receiver of the call
						 * in question is implemented by the code object whose
						 * parameter we are looking at.
						 */
						return ObjectIsRelevant.NOT_A_CLUE;
					}
				});
	}
}

final class CallPackager
		implements
		Transformer<TCallable, Result<uk.ac.ic.doc.gander.flowinference.call.Call>> {
	private final ModelSite<Call> senderCallSite;
	private final InvokableCodeObject invokable;

	CallPackager(InvokableCodeObject invokable, ModelSite<Call> senderCallSite) {
		this.invokable = invokable;
		this.senderCallSite = senderCallSite;
	}

	@Override
	public Result<uk.ac.ic.doc.gander.flowinference.call.Call> transformFiniteResult(
			Set<TCallable> relevantCallables) {

		Set<uk.ac.ic.doc.gander.flowinference.call.Call> calls = new HashSet<uk.ac.ic.doc.gander.flowinference.call.Call>();

		for (TCallable callable : relevantCallables) {
			calls.add(new DefaultCall(invokable, callable, senderCallSite));
		}

		return new FiniteResult<uk.ac.ic.doc.gander.flowinference.call.Call>(
				calls);
	}

	@Override
	public Result<uk.ac.ic.doc.gander.flowinference.call.Call> transformInfiniteResult() {
		return new Top<uk.ac.ic.doc.gander.flowinference.call.Call>() {

			@Override
			public String toString() {
				return "⊤call";
			}
		};
	}
}

final class CallDancer implements
		Transformer<uk.ac.ic.doc.gander.flowinference.call.Call, Result<Type>> {

	private final Variable variable;
	private final SubgoalManager goalManager;

	public CallDancer(Variable variable, SubgoalManager goalManager) {
		this.variable = variable;
		this.goalManager = goalManager;
	}

	@Override
	public Result<Type> transformFiniteResult(
			Set<uk.ac.ic.doc.gander.flowinference.call.Call> calls) {

		RedundancyEliminator<Type> type = new RedundancyEliminator<Type>();

		for (uk.ac.ic.doc.gander.flowinference.call.Call call : calls) {

			Result<Argument> arguments = call.argumentsBoundToVariable(
					variable, goalManager);

			type.add(arguments.transformResult(new ArgumentTyper(goalManager)));
			if (type.isFinished())
				break;
		}

		return type.result();
	}

	@Override
	public Result<Type> transformInfiniteResult() {
		return TopT.INSTANCE;
	}
}

final class ArgumentTyper implements Transformer<Argument, Result<Type>> {

	private final SubgoalManager goalManager;

	public ArgumentTyper(SubgoalManager goalManager) {
		this.goalManager = goalManager;
	}

	@Override
	public Result<Type> transformFiniteResult(Set<Argument> arguments) {

		RedundancyEliminator<Type> type = new RedundancyEliminator<Type>();

		for (Argument argument : arguments) {

			type.add(argument.type(goalManager));
			if (type.isFinished())
				break;
		}

		return type.result();
	}

	@Override
	public Result<Type> transformInfiniteResult() {
		return TopT.INSTANCE;
	}
}
