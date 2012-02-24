package uk.ac.ic.doc.gander.model.parameters;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.callsite.InternalCallsite;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow.ExpressionPosition;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

final class NamedParameter implements FormalParameter {

	private final int index;
	private final ModelSite<exprType> defaultValue;
	private final ModelSite<Name> parameter;

	@Override
	public InvokableCodeObject codeObject() {
		return (InvokableCodeObject) parameter.codeObject();
	}

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
		if (!(parameter.codeObject() instanceof InvokableCodeObject))
			throw new IllegalArgumentException("Parameters can only occur "
					+ "in in invokable code object");

		this.index = parameterIndex;
		this.parameter = parameter;
		this.defaultValue = defaultValue; // may be null
	}

	@Override
	public Set<Argument> argumentsPassedAtCall(InternalCallsite callSite,
			SubgoalManager goalManager) {

		/*
		 * Positional arguments are given a chance to pass to this parameter
		 * first
		 */
		Argument argument = callSite.argumentExplicitlyPassedAtPosition(index);
		if (argument == null) {

			/* Next explicit keywords get a go */
			argument = callSite.argumentExplicitlyPassedToKeyword(name());

		} else if (callSite.argumentExplicitlyPassedToKeyword(name()) != null) {

			System.err.println("PROGRAM ERROR: cannot pass an argument "
					+ "by position and keyword to the same parameter");
		}

		if (argument != null) {
			return Collections.singleton(argument);
		} else {

			/*
			 * When neither an explicit positional nor keyword argument is
			 * passed, any of the remaining argument types could potentially
			 * arrive at this parameter (it is decided at runtime) so we combine
			 * them.
			 */

			Set<Argument> arguments = new HashSet<Argument>();

			/* Perhaps an argument was passed using an unpacked iterable */
			argument = callSite.argumentThatCouldExpandIntoPosition(index);
			if (argument != null) {
				arguments.add(argument);
			}

			/* Or perhaps there's an unpacked dictionary argument */
			argument = callSite.argumentThatCouldExpandIntoKeyword(name());
			if (argument != null) {
				arguments.add(argument);
			}

			/*
			 * We've exhausted arguments passed at the callsite. Now default
			 * arguments come into play.
			 */
			if (defaultValue() != null) {
				arguments.add(new DefaultArgument(defaultValue()));
			} else {
				/*
				 * Big no-no. We've run out of argument and there is no default
				 * to plug the gap. The program is wrong.
				 */
				System.err
						.println("PROGRAM ERROR: Too few arguments passed to "
								+ parameter.codeObject() + " at " + callSite);
			}

			return arguments;
		}

	}

	/**
	 * All arguments passed to named parameters flow in the same way; to the
	 * named parameter.
	 */
	@Override
	public ArgumentDestination passage(Argument argument) {

		return new ArgumentDestination() {

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
	public boolean acceptsArgumentByPosition(int position) {
		return position == index;
	}

	@Override
	public boolean acceptsArgumentByKeyword(String keyword) {
		return keyword.equals(name());
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
