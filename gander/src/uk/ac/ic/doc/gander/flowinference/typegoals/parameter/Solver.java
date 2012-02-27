package uk.ac.ic.doc.gander.flowinference.typegoals.parameter;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.call.CallDispatch;
import uk.ac.ic.doc.gander.flowinference.call.TopD;
import uk.ac.ic.doc.gander.flowinference.callframe.CallSiteStackFrame;
import uk.ac.ic.doc.gander.flowinference.callframe.StackFrame;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator.DatumProcessor;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.sendersgoals.FunctionSendersGoal;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.flowinference.typegoals.expression.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.types.TCallable;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

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
		 * But now we have all the objects syntactic call-site. Some of these
		 * may invoke more than just our code object (an object whose call
		 * reciever is decidable at runtime such as __init__ or __call__). Some
		 * may not invoke our code object at all (a polymorphic call-site).
		 * 
		 * The calls we care about are those that are dispatched to our code
		 * object. We extract those now.
		 */
		Result<CallDispatch> calls = extractRelevantCalls(callSiteTypes,
				senderCallSite);

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
		 * CallDispatch object that takes care of coordinating the dance.
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

	private Result<CallDispatch> extractRelevantCalls(
			Result<Type> callSiteTypes, ModelSite<Call> senderCallSite) {

		return callSiteTypes.transformResult(new RelevantCallFilter(invokable,
				senderCallSite, goalManager));
	}

	private Result<Type> deriveParameterTypeFromCalls(Result<CallDispatch> calls) {

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
 * Extracts only relevant call from the callable objects.
 */
final class RelevantCallFilter implements
		Transformer<Type, Result<CallDispatch>> {

	private final InvokableCodeObject invokable;
	private final SubgoalManager goalManager;
	private final ModelSite<Call> senderCallSite;

	RelevantCallFilter(InvokableCodeObject invokable,
			ModelSite<Call> senderCallSite, SubgoalManager goalManager) {
		assert senderCallSite != null;
		assert invokable != null;
		assert goalManager != null;

		this.invokable = invokable;
		this.senderCallSite = senderCallSite;
		this.goalManager = goalManager;
	}

	@Override
	public Result<CallDispatch> transformFiniteResult(
			Set<Type> objectsAtCallSite) {

		RedundancyEliminator<CallDispatch> calls = new RedundancyEliminator<CallDispatch>();

		for (Type calledObject : objectsAtCallSite) {

			if (calledObject instanceof TCallable) {

				calls.add(callsThatMightInvokeOurCodeObject((TCallable) calledObject));

			} else {
				System.err.println("UNTYPABLE: call site isn't "
						+ "calling a callable: " + calledObject);
			}

			if (calls.isFinished())
				break;
		}

		return calls.result();
	}

	@Override
	public Result<CallDispatch> transformInfiniteResult() {
		/*
		 * No idea how we are being called.
		 */
		return TopD.INSTANCE;
		/*
		 * TODO: could take the union of the types of all the arguments passed
		 * at the callsite as it is only which of them that gets passed that we
		 * don't know
		 */
	}

	private Result<CallDispatch> callsThatMightInvokeOurCodeObject(
			TCallable object) {

		StackFrame<Argument> call = new CallSiteStackFrame(senderCallSite);

		return object.dispatches(call, goalManager).transformResult(
				new Transformer<CallDispatch, Result<CallDispatch>>() {

					@Override
					public Result<CallDispatch> transformFiniteResult(
							Set<CallDispatch> dispatches) {

						Set<CallDispatch> relevantDispatches = new HashSet<CallDispatch>();

						for (CallDispatch dispatch : dispatches) {

							if (dispatch.receiver().equals(invokable)) {
								relevantDispatches.add(dispatch);
							}
						}

						return new FiniteResult<CallDispatch>(
								relevantDispatches);
					}

					@Override
					public Result<CallDispatch> transformInfiniteResult() {
						/*
						 * We can't tell if this possible receiver of the call
						 * in question is implemented by the code object whose
						 * parameter we are looking at.
						 */
						return TopD.INSTANCE;
					}
				});
	}
}

final class CallDancer implements Transformer<CallDispatch, Result<Type>> {

	private final Variable variable;
	private final SubgoalManager goalManager;

	public CallDancer(Variable variable, SubgoalManager goalManager) {
		this.variable = variable;
		this.goalManager = goalManager;
	}

	@Override
	public Result<Type> transformFiniteResult(Set<CallDispatch> calls) {

		RedundancyEliminator<Type> type = new RedundancyEliminator<Type>();

		for (CallDispatch call : calls) {

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
