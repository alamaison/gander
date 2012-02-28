package uk.ac.ic.doc.gander.flowinference.argument;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;

final class ExpandedMapArgument implements KeywordArgument {

	private final ModelSite<exprType> argument;

	ExpandedMapArgument(ModelSite<exprType> argument) {
		assert argument != null;
		this.argument = argument;
	}

	@Override
	public ArgumentDestination passArgumentAtCall(InvokableCodeObject receiver) {

		return new ArgumentDestination() {

			@Override
			public Result<FlowPosition> nextFlowPositions() {
				/*
				 * Expanding a mapping in call doesn't flow the mapping
				 * anywhere, just its values
				 */
				return FiniteResult.bottom();
			}
		};
	}

	@Override
	public Result<Type> type(SubgoalManager goalManager) {
		return TopT.INSTANCE;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((argument == null) ? 0 : argument.hashCode());
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
		ExpandedMapArgument other = (ExpandedMapArgument) obj;
		if (argument == null) {
			if (other.argument != null)
				return false;
		} else if (!argument.equals(other.argument))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ExpandedMapArgument [argument=" + argument + "]";
	}

}
