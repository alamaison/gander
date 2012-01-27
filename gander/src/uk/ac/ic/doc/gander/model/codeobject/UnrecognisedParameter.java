package uk.ac.ic.doc.gander.model.codeobject;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.model.ModelSite;

final class UnrecognisedParameter implements FormalParameter {

	private final ModelSite<exprType> parameter;

	UnrecognisedParameter(ModelSite<exprType> parameter) {
		this.parameter = parameter;
	}

	@Override
	public ModelSite<? extends exprType> parameterSite() {
		return parameter;
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

}