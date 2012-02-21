package uk.ac.ic.doc.gander.flowinference.argument;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.keywordType;

import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * Models an argument at a callsite that is passed by keyword as an individual
 * argument.
 * 
 * The simplest example would look like {@code f(a_keyword=this_argument)}.
 * 
 * The argument is not passed as part of an expanded dictionary object. In other
 * words, not {@code f(**dictionary)}.
 */
final class ExplicitKeywordCallsiteArgument implements KeywordCallsiteArgument {

	private final ModelSite<Call> callSite;
	private final keywordType keyword;

	ExplicitKeywordCallsiteArgument(ModelSite<Call> callSite, int keywordIndex) {
		this.callSite = callSite;
		this.keyword = callSite.astNode().keywords[keywordIndex];
	}

	@Override
	public Argument mapToActualArgument(ArgumentPassingStrategy argumentMapper) {
		return new ExplicitKeywordArgument(callSite, keyword);
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
		ExplicitKeywordCallsiteArgument other = (ExplicitKeywordCallsiteArgument) obj;
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
		return "ExplicitKeywordCallsiteArgument [callSite=" + callSite
				+ ", keyword=" + keyword + "]";
	}

}
