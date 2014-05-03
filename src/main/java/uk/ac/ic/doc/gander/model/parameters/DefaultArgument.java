package uk.ac.ic.doc.gander.model.parameters;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.expression.ExpressionTypeGoal;
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
	public Result<PyObject> type(SubgoalManager goalManager) {
		return goalManager
				.registerSubgoal(new ExpressionTypeGoal(defaultValue));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((defaultValue == null) ? 0 : defaultValue.hashCode());
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
		DefaultArgument other = (DefaultArgument) obj;
		if (defaultValue == null) {
			if (other.defaultValue != null)
				return false;
		} else if (!defaultValue.equals(other.defaultValue))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DefaultArgument [defaultValue=" + defaultValue + "]";
	}
}
