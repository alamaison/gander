package uk.ac.ic.doc.gander.flowinference;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.keywordType;

import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.FormalParameter;
import uk.ac.ic.doc.gander.model.codeobject.FormalParameters;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;

public final class ExplicitKeywordArgument implements KeywordArgument {

	private final ModelSite<Call> callSite;
	private final keywordType keyword;

	public ExplicitKeywordArgument(ModelSite<Call> callSite, int keywordIndex) {
		this.callSite = callSite;
		this.keyword = callSite.astNode().keywords[keywordIndex];
	}

	@Override
	public ArgumentPassage passArgumentAtCall(
			final InvokableCodeObject receiver,
			ArgumentPassingStrategy argumentMapper) {

		FormalParameters parameters = receiver.formalParameters();

		if (parameters.hasParameterName(keyword())) {

			FormalParameter parameter = parameters.namedParameter(keyword());
			return parameter.passage(this);

		} else {
			return new UntypableArgumentPassage() {

				@Override
				public Result<FlowPosition> nextFlowPositions() {
					System.err.println("UNTYPABLE: " + receiver
							+ " has no parameter that accepts a keyword "
							+ "argument called '" + keyword() + "'");

					return FiniteResult.bottom();
				}
			};
		}
	}

	String keyword() {
		return ((NameTok) keyword.arg).id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((callSite == null) ? 0 : callSite.hashCode());
		result = prime * result + ((keyword == null) ? 0 : keyword.hashCode());
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
		ExplicitKeywordArgument other = (ExplicitKeywordArgument) obj;
		if (callSite == null) {
			if (other.callSite != null)
				return false;
		} else if (!callSite.equals(other.callSite))
			return false;
		if (keyword == null) {
			if (other.keyword != null)
				return false;
		} else if (!keyword.equals(other.keyword))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ExplicitKeywordArgument [callSite=" + callSite + ", keyword="
				+ keyword + "]";
	}

}
