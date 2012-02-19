package uk.ac.ic.doc.gander.model.codeobject;

import java.util.Collections;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.ArgumentPassage;
import uk.ac.ic.doc.gander.flowinference.KeywordArgument;
import uk.ac.ic.doc.gander.flowinference.PositionalArgument;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow.ExpressionPosition;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

public final class NamedParameter implements FormalParameter {

	private final int index;
	private final ModelSite<exprType> defaultValue;
	private final ModelSite<Name> parameter;

	public int index() {
		return index;
	}

	public ModelSite<exprType> defaultValue() {
		return defaultValue;
	}

	public String name() {
		return parameter.astNode().id;
	}

	NamedParameter(int parameterIndex, ModelSite<Name> parameter,
			ModelSite<exprType> defaultValue) {
		if (parameterIndex < 0)
			throw new IllegalArgumentException("Invalid index index: "
					+ parameterIndex);
		if (parameter == null)
			throw new NullPointerException("Parameter required");
		if (parameter.astNode().id == null)
			throw new IllegalArgumentException("Parameter must have name");
		if (parameter.astNode().id.isEmpty())
			throw new IllegalArgumentException(
					"Parameter name must contain characters");

		this.index = parameterIndex;
		this.parameter = parameter;
		this.defaultValue = defaultValue; // may be null
	}

	@Override
	public ModelSite<Name> site() {
		return parameter;
	}

	@Override
	public Result<Type> typeAtCall(ModelSite<Call> callSite,
			SubgoalManager goalManager) {
		ModelSite<exprType> passedArgument = expressionFromArgumentList(
				callSite, index());
		if (passedArgument != null) {
			return goalManager.registerSubgoal(new ExpressionTypeGoal(
					passedArgument));
		} else {
			return TopT.INSTANCE;
		}
	}

	private ModelSite<exprType> expressionFromArgumentList(
			ModelSite<Call> callSite, int index) {

		if (index < callSite.astNode().args.length) {

			return new ModelSite<exprType>(callSite.astNode().args[index],
					callSite.codeObject());

		} else {
			/*
			 * Fewer argument were passed to the function than are declared in
			 * its signature. It's probably expecting default arguments.
			 */
			ModelSite<exprType> defaultValue = defaultValue();
			if (defaultValue != null) {
				return defaultValue;
			} else {
				/* No default. The program is wrong. */
				System.err
						.println("PROGRAM ERROR: Too few arguments passed to "
								+ parameter.codeObject() + " at " + callSite);
				return null;
			}
		}
	}

	@Override
	public ArgumentPassage passage(PositionalArgument argument) {
		return passage();
	}

	@Override
	public ArgumentPassage passage(KeywordArgument argument) {
		return passage();
	}

	/**
	 * All arguments passed to named parameters flow in the same way; to the
	 * named parameter.
	 */
	private ArgumentPassage passage() {

		return new ArgumentPassage() {

			@Override
			public Result<FlowPosition> nextFlowPositions() {
				return new FiniteResult<FlowPosition>(
						Collections
								.<FlowPosition> singleton(new ExpressionPosition(
										parameter)));
			}
		};
	}

	@Override
	public Set<Variable> boundVariables() {
		return Collections.singleton(new Variable(parameter.astNode().id,
				parameter.codeObject()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((parameter == null) ? 0 : parameter.hashCode());
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
		NamedParameter other = (NamedParameter) obj;
		if (parameter == null) {
			if (other.parameter != null)
				return false;
		} else if (!parameter.equals(other.parameter))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NamedParameter [parameter=" + parameter + ", index=" + index
				+ ", defaultValue=" + defaultValue + "]";
	}

}
