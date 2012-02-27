package uk.ac.ic.doc.gander.flowinference.argument;

import java.util.Collections;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.types.TObject;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.parameters.FormalParameter;

public final class SelfArgument implements PositionalArgument {

	private final int selfPosition;
	private final TObject instance;

	public SelfArgument(int selfPosition, TObject instance) {
		this.selfPosition = selfPosition;
		this.instance = instance;
	}

	@Override
	public ArgumentDestination passArgumentAtCall(InvokableCodeObject receiver) {

		FormalParameter parameter = receiver.formalParameters().passByPosition(
				selfPosition);
		return parameter.passage(this);
	}

	@Override
	public boolean isPassedAtPosition(int position) {
		return position == this.selfPosition;
	}

	@Override
	public boolean isPassedByKeyword(String keyword) {
		return false;
	}

	@Override
	public boolean mayExpandIntoPosition() {
		return false;
	}

	@Override
	public boolean mayExpandIntoKeyword() {
		return false;
	}

	@Override
	public Result<Type> type(SubgoalManager goalManager) {
		return new FiniteResult<Type>(Collections.singleton(instance));
	}
}
