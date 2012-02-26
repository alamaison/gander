package uk.ac.ic.doc.gander.flowinference.argument;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.expression.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.parameters.FormalParameter;
import uk.ac.ic.doc.gander.model.parameters.FormalParameters;

/**
 * Model an argument, internal to the interpreter, that is being passed to a
 * procedure using a numeric position to map it to a parameter.
 */
final class ExplicitPositionalArgument implements PositionalArgument {

	private final int position;
	private final ModelSite<? extends exprType> value;

	ExplicitPositionalArgument(ModelSite<? extends exprType> value, int position) {
		assert value != null;
		assert position >= 0;

		this.value = value;
		this.position = position;
	}

	/**
	 * Pass argument to parameters once position has been adjusted for
	 * particular calling mechanism.
	 */
	@Override
	public ArgumentDestination passArgumentAtCall(
			final InvokableCodeObject receiver) {

		FormalParameters parameters = receiver.formalParameters();

		if (parameters.hasParameterForPosition(position)) {

			FormalParameter parameter = parameters.passByPosition(position);
			return parameter.passage(this);

		} else {
			return new UntypableArgumentDestination() {

				@Override
				public Result<FlowPosition> nextFlowPositions() {
					System.err.println("UNTYPABLE: " + receiver
							+ " has no parameter that accepts an argument "
							+ "at position " + position);

					return FiniteResult.bottom();
				}
			};
		}
	}

	@Override
	public boolean isPassedAtPosition(int position) {
		return this.position == position;
	}

	@Override
	public boolean isPassedByKeyword(String keyword) {
		return false;
	}

	@Override
	public boolean mayExpandIntoPosition(int position) {
		return false;
	}

	@Override
	public boolean mayExpandIntoKeyword(String keyword) {
		return false;
	}

	@Override
	public Result<Type> type(SubgoalManager goalManager) {
		return goalManager.registerSubgoal(new ExpressionTypeGoal(value));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ExplicitPositionalArgument [position=" + position + ", value="
				+ value + "]";
	}

}
