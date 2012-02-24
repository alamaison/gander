package uk.ac.ic.doc.gander.flowinference.argument;

import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.exprType;
import org.python.pydev.parser.jython.ast.keywordType;

import uk.ac.ic.doc.gander.flowinference.callsite.ArgumentPassingStrategy;
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

	private final ModelSite<keywordType> argument;

	ExplicitKeywordCallsiteArgument(ModelSite<keywordType> argument) {
		assert argument != null;
		this.argument = argument;
	}

	@Override
	public Argument mapToActualArgument(ArgumentPassingStrategy argumentMapper) {
		keywordType node = argument.astNode();
		String keyword = ((NameTok) node.arg).id;
		ModelSite<exprType> value = new ModelSite<exprType>(node.value,
				argument.codeObject());

		return new ExplicitKeywordArgument(keyword, value);
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
		ExplicitKeywordCallsiteArgument other = (ExplicitKeywordCallsiteArgument) obj;
		if (argument == null) {
			if (other.argument != null)
				return false;
		} else if (!argument.equals(other.argument))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ExplicitKeywordCallsiteArgument [argument=" + argument + "]";
	}

}
