package uk.ac.ic.doc.gander.model;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.keywordType;

import uk.ac.ic.doc.gander.model.codeobject.FormalParameter;

public final class KeywordArgument implements Argument {

	private final ModelSite<Call> callSite;
	private final keywordType keyword;

	public KeywordArgument(ModelSite<Call> callSite, int keywordIndex) {
		this.callSite = callSite;
		this.keyword = callSite.astNode().keywords[keywordIndex];
	}

	@Override
	public FormalParameter passArgumentAtCall(CallArgumentMapper argumentMapper) {
		return argumentMapper
				.namedParameter(((NameTok) keyword.arg).id);
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
		KeywordArgument other = (KeywordArgument) obj;
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
		return "KeywordArgument [callSite=" + callSite + ", keyword=" + keyword
				+ "]";
	}
	
}
