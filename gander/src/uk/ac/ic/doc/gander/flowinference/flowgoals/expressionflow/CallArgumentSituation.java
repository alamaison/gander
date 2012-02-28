package uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow;

import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.argument.CallsiteArgument;
import uk.ac.ic.doc.gander.flowinference.call.CallDispatch;
import uk.ac.ic.doc.gander.flowinference.callframe.CallSiteStackFrame;
import uk.ac.ic.doc.gander.flowinference.callframe.StackFrame;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopFp;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.result.Top;
import uk.ac.ic.doc.gander.flowinference.typegoals.expression.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.types.FunctionStylePassingStrategy;
import uk.ac.ic.doc.gander.flowinference.types.TCallable;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;

final class CallArgumentSituation implements FlowSituation {

	private final class ArgumentDispatcher implements
			Transformer<CallDispatch, Result<ArgumentDestination>> {
		private final SubgoalManager goalManager;

		private ArgumentDispatcher(SubgoalManager goalManager) {
			this.goalManager = goalManager;
		}

		@Override
		public Result<ArgumentDestination> transformFiniteResult(
				Set<CallDispatch> result) {

			RedundancyEliminator<ArgumentDestination> destinations = new RedundancyEliminator<ArgumentDestination>();

			for (CallDispatch callDispatch : result) {
				destinations.add(callDispatch.destinationsReceivingArgument(
						argument(), goalManager));

				if (destinations.isFinished())
					break;
			}

			return destinations.result();
		}

		@Override
		public Result<ArgumentDestination> transformInfiniteResult() {
			return new Top<ArgumentDestination>() {

				@Override
				public String toString() {
					return "blah";
				}
			};
		}
	}

	private final ModelSite<Call> callSite;
	private final CallsiteArgument argument;
	private final StackFrame<Argument> stackFrame;

	public CallArgumentSituation(ModelSite<Call> callSite,
			CallsiteArgument argument) {
		this.callSite = callSite;
		this.argument = argument;

		stackFrame = new CallSiteStackFrame(callSite);
	}

	@Override
	public Result<FlowPosition> nextFlowPositions(
			final SubgoalManager goalManager) {
		Result<Type> receivers = goalManager
				.registerSubgoal(new ExpressionTypeGoal(
						new ModelSite<exprType>(callSite.astNode().func,
								callSite.codeObject())));

		return receivers
				.transformResult(new Transformer<Type, Result<FlowPosition>>() {

					@Override
					public Result<FlowPosition> transformFiniteResult(
							Set<Type> receiverTypes) {
						return nextPositions(receiverTypes, goalManager);
					}

					@Override
					public Result<FlowPosition> transformInfiniteResult() {
						/*
						 * We've lost track of where the argument flows to so no
						 * choice but to surrender.
						 */
						return TopFp.INSTANCE;
					}
				});
	}

	private Result<FlowPosition> nextPositions(Set<Type> receiverTypes,
			SubgoalManager goalManager) {
		RedundancyEliminator<FlowPosition> nextPositions = new RedundancyEliminator<FlowPosition>();

		for (Type receiver : receiverTypes) {
			nextPositions.add(parametersOf(receiver, goalManager));
			if (nextPositions.isFinished())
				break;
		}

		return nextPositions.result();
	}

	/**
	 * Finds the flow positions that the argument might flow when invoking a
	 * particular receiver.
	 */
	private Result<FlowPosition> parametersOf(Type receiver,
			SubgoalManager goalManager) {

		if (receiver instanceof TCallable) {

			Result<CallDispatch> calls = ((TCallable) receiver).dispatches(
					stackFrame, goalManager);

			Result<ArgumentDestination> receivingParameters = calls
					.transformResult(new ArgumentDispatcher(goalManager));

			return receivingParameters
					.transformResult(new ReceivingParameterPositioner());
		} else {
			/*
			 * XXX: just because the analysis thinks this might be happening,
			 * doesn't mean that it will. Code might be correct in practice.
			 */
			System.err
					.println("UNTYPABLE: Can't call non-callable code object");
			return FiniteResult.bottom();
		}
	}

	private Argument argument() {
		return argument.mapToActualArgument(new FunctionStylePassingStrategy());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((argument == null) ? 0 : argument.hashCode());
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
		CallArgumentSituation other = (CallArgumentSituation) obj;
		if (argument == null) {
			if (other.argument != null)
				return false;
		} else if (!argument.equals(other.argument))
			return false;
		if (callSite == null) {
			if (other.callSite != null)
				return false;
		} else if (!callSite.equals(other.callSite))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CallArgumentSituation [argument=" + argument + ", callSite="
				+ callSite + "]";
	}

}
