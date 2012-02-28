package uk.ac.ic.doc.gander.model.parameters;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.expression.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;

final class DefaultArgument implements Argument {

	private final ModelSite<exprType> defaultValue;

	DefaultArgument(ModelSite<exprType> defaultValue) {
		if (defaultValue == null)
			throw new NullPointerException(
					"Default argument must have an expression to evaluate");
		this.defaultValue = defaultValue;
	}

	@Override
	public ArgumentDestination passArgumentAtCall(InvokableCodeObject receiver) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<Type> type(SubgoalManager goalManager) {
		return goalManager
				.registerSubgoal(new ExpressionTypeGoal(defaultValue));
	}
}
