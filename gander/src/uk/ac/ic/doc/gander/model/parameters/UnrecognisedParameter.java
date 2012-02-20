package uk.ac.ic.doc.gander.model.parameters;

import java.util.Collections;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.argument.ArgumentPassage;
import uk.ac.ic.doc.gander.flowinference.argument.KeywordArgument;
import uk.ac.ic.doc.gander.flowinference.argument.PositionalArgument;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopFp;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

final class UnrecognisedParameter implements FormalParameter {

	private final ModelSite<exprType> parameter;

	UnrecognisedParameter(ModelSite<exprType> parameter) {
		this.parameter = parameter;
	}

	@Override
	public ModelSite<? extends exprType> site() {
		return parameter;
	}

	@Override
	public ArgumentPassage passage(PositionalArgument argument) {
		return passage();
	}

	@Override
	public ArgumentPassage passage(KeywordArgument argument) {
		return passage();
	}

	@Override
	public Result<Type> typeAtCall(ModelSite<Call> callSite,
			SubgoalManager goalManager) {
		return TopT.INSTANCE;
	}

	private ArgumentPassage passage() {

		return new ArgumentPassage() {

			@Override
			public Result<FlowPosition> nextFlowPositions() {
				return TopFp.INSTANCE;
			}
		};
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
		UnrecognisedParameter other = (UnrecognisedParameter) obj;
		if (parameter == null) {
			if (other.parameter != null)
				return false;
		} else if (!parameter.equals(other.parameter))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "UnrecognisedParameter [parameter=" + parameter + "]";
	}

	@Override
	public Set<Variable> boundVariables() {
		return Collections.emptySet();
	}

}