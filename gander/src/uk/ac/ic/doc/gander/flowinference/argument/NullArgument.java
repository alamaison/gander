package uk.ac.ic.doc.gander.flowinference.argument;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;

/**
 * A special argument that really signifies a value that isn't passed at all.
 */
enum NullArgument implements Argument {

	INSTANCE;

	@Override
	public ArgumentDestination passArgumentAtCall(InvokableCodeObject receiver) {

		return new ArgumentDestination() {

			@Override
			public Result<FlowPosition> nextFlowPositions() {
				return FiniteResult.bottom();
			}
		};

	}

	@Override
	public boolean isPassedAtPosition(int position) {
		return false;
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
		return FiniteResult.bottom();
	}

}