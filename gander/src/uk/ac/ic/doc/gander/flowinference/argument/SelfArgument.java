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
	public Result<Type> type(SubgoalManager goalManager) {
		return new FiniteResult<Type>(Collections.singleton(instance));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((instance == null) ? 0 : instance.hashCode());
		result = prime * result + selfPosition;
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
		SelfArgument other = (SelfArgument) obj;
		if (instance == null) {
			if (other.instance != null)
				return false;
		} else if (!instance.equals(other.instance))
			return false;
		if (selfPosition != other.selfPosition)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SelfArgument [instance=" + instance + "]";
	}

}
