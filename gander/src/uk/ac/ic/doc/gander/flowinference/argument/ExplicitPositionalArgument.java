package uk.ac.ic.doc.gander.flowinference.argument;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.parameters.FormalParameter;
import uk.ac.ic.doc.gander.model.parameters.FormalParameters;

final class ExplicitPositionalArgument implements PositionalArgument {

	private final ModelSite<Call> callSite;
	private final int position;

	ExplicitPositionalArgument(ModelSite<Call> callSite, int position) {
		this.callSite = callSite;
		this.position = position;
	}

	/**
	 * Pass argument to parameters once position has been adjusted for
	 * particular calling mechanism.
	 */
	@Override
	public ArgumentPassage passArgumentAtCall(
			final InvokableCodeObject receiver,
			ArgumentPassingStrategy argumentMapper) {

		FormalParameters parameters = receiver.formalParameters();

		final int realPosition = argumentMapper.realPosition(position);

		if (parameters.hasParameterForPosition(position)) {

			FormalParameter parameter = parameters.passByPosition(realPosition);
			return parameter.passage(this);

		} else {
			return new UntypableArgumentPassage() {

				@Override
				public Result<FlowPosition> nextFlowPositions() {
					System.err.println("UNTYPABLE: " + receiver
							+ " has no parameter that accepts an argument "
							+ "at position " + realPosition);

					return FiniteResult.bottom();
				}
			};
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + position;
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
		ExplicitPositionalArgument other = (ExplicitPositionalArgument) obj;
		if (position != other.position)
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
		return "ExplicitPositionalArgument [callSite=" + callSite
				+ ", position=" + position + "]";
	}

}
