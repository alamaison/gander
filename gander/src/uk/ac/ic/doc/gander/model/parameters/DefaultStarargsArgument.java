package uk.ac.ic.doc.gander.model.parameters;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;

final class DefaultStarargsArgument implements Argument {

	@Override
	public ArgumentDestination passArgumentAtCall(InvokableCodeObject receiver) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPassedAtPosition(int position) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPassedByKeyword(String keyword) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mayExpandIntoPosition(int position) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mayExpandIntoKeyword(String keyword) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Result<Type> type(SubgoalManager goalManager) {
		// TODO: return builtin tuple
		return TopT.INSTANCE;
	}

}
