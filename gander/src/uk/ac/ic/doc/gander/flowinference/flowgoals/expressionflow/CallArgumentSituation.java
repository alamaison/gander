package uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopFp;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.typegoals.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.types.TCallable;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Argument;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.FormalParameter;

final class CallArgumentSituation implements FlowSituation {

	private final ModelSite<Call> callSite;
	private final Argument argument;

	public CallArgumentSituation(ModelSite<Call> callSite, Argument argument) {
		this.callSite = callSite;
		this.argument = argument;
	}

	@Override
	public Result<FlowPosition> nextFlowPositions(final SubgoalManager goalManager) {
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

	private Result<FlowPosition> nextPositions(Set<Type> receiverTypes, SubgoalManager goalManager) {
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

			Result<FormalParameter> receivingParameters = ((TCallable) receiver)
					.formalParametersReceivingArgument(argument(), goalManager);

			return receivingParameters
					.transformResult(new ReceivingParameterPositioner());
		} else {
			/*
			 * XXX: just because the analysis thinks this might be happening,
			 * doesn't mean that it will. Code might be correct in practice.
			 */
			throw new RuntimeException("Can't call non-callable code object");
		}
	}

	/**
	 * Turns formal parameters into flow positions.
	 */
	private final class ReceivingParameterPositioner implements
			Transformer<FormalParameter, Result<FlowPosition>> {

		@Override
		public Result<FlowPosition> transformFiniteResult(
				Set<FormalParameter> receivingParameters) {

			Set<FlowPosition> parameterPositions = new HashSet<FlowPosition>();

			for (FormalParameter parameter : receivingParameters) {
				parameterPositions.add(new ExpressionPosition(parameter
						.site()));
			}

			return new FiniteResult<FlowPosition>(parameterPositions);
		}

		@Override
		public Result<FlowPosition> transformInfiniteResult() {
			return TopFp.INSTANCE;
		}
	}

	private Argument argument() {
		return argument;
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
