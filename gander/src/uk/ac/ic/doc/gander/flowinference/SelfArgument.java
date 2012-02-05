package uk.ac.ic.doc.gander.flowinference;

import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.codeobject.FormalParameter;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;

public final class SelfArgument implements PositionalArgument {

	@Override
	public ArgumentPassage passArgumentAtCall(InvokableCodeObject receiver,
			ArgumentPassingStrategy argumentMapper) {
		
		if (argumentMapper.passesHiddenSelf()) {
			
			FormalParameter parameter = receiver.formalParameters()
					.passByPosition(argumentMapper.selfPosition());
			return parameter.passage(this);
			
		} else {
			
			/*
			 * If the passing strategy doesn't pass a self argument, it won't
			 * have any new flow positions.
			 */
			return new ArgumentPassage() {
				@Override
				public Result<FlowPosition> nextFlowPositions() {
					return FiniteResult.bottom();
				}
			};
		}
	}

}
