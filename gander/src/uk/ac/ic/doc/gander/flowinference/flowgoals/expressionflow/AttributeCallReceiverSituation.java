package uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Attribute;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopFp;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.typegoals.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.types.TCallable;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.FormalParameter;

final class AttributeCallReceiverSituation implements FlowSituation {

	private final ModelSite<Attribute> receiver;

	AttributeCallReceiverSituation(ModelSite<Attribute> receiver) {
		this.receiver = receiver;
	}

	@Override
	public Result<FlowPosition> nextFlowPositions(
			final SubgoalManager goalManager) {

		Result<Type> receivers = goalManager
				.registerSubgoal(new ExpressionTypeGoal(receiver));

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
		Set<FlowPosition> nextPositions = new HashSet<FlowPosition>();

		for (Type receiver : receiverTypes) {
			FlowPosition self = selfParameterOf(receiver, goalManager);
			if (self != null) {
				nextPositions.add(self);
			}
		}

		return new FiniteResult<FlowPosition>(nextPositions);
	}

	/**
	 * Finds the flow position that the attribute LHS might flow when the
	 * attribute is the receiver of a call.
	 */
	private FlowPosition selfParameterOf(Type receiver,
			SubgoalManager goalManager) {

		if (receiver instanceof TCallable) {

			FormalParameter self = ((TCallable) receiver).selfParameter();
			if (self != null) {
				return new ExpressionPosition(self.parameterSite());
			} else {
				return null;
			}

		} else {
			/*
			 * XXX: just because the analysis thinks this might be happening,
			 * doesn't mean that it will. Code might be correct in practice.
			 */
			throw new RuntimeException("Can't call non-callable code object");
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((receiver == null) ? 0 : receiver.hashCode());
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
		AttributeCallReceiverSituation other = (AttributeCallReceiverSituation) obj;
		if (receiver == null) {
			if (other.receiver != null)
				return false;
		} else if (!receiver.equals(other.receiver))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AttributeCallReceiverSituation [receiver=" + receiver + "]";
	}

}
