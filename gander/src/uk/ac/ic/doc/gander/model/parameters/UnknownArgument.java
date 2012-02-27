package uk.ac.ic.doc.gander.model.parameters;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;

enum UnknownArgument implements Argument {
	INSTANCE;
	
	@Override
	public Result<Type> type(SubgoalManager goalManager) {
		return TopT.INSTANCE;
	}

	@Override
	public ArgumentDestination passArgumentAtCall(
			InvokableCodeObject receiver) {
		return new ArgumentDestination() {

			@Override
			public Result<FlowPosition> nextFlowPositions() {
				return FiniteResult.bottom();
			}
		};
	}

	@Override
	public boolean mayExpandIntoPosition() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mayExpandIntoKeyword() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPassedByKeyword(String keyword) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPassedAtPosition(int position) {
		// TODO Auto-generated method stub
		return false;
	}
}