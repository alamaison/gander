package uk.ac.ic.doc.gander.flowinference.argument;

import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.parameters.FormalParameter;

public final class SelfArgument implements PositionalArgument {

	private final int selfPosition;

	public SelfArgument(int selfPosition) {
		this.selfPosition = selfPosition;
	}

	@Override
	public ArgumentDestination passArgumentAtCall(InvokableCodeObject receiver) {

		FormalParameter parameter = receiver.formalParameters().passByPosition(
				selfPosition);
		return parameter.passage(this);
	}

}
