package uk.ac.ic.doc.gander.model.codeobject;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.model.ModelSite;

public final class NamedParameter {

	private final int index;
	private final String parameterName;
	private final ModelSite<exprType> defaultValue;
	
	public int index() {
		return index;
	}

	public ModelSite<exprType> defaultValue() {
		return defaultValue;
	}

	NamedParameter(int parameterIndex, String parameterName,
			ModelSite<exprType> defaultValue) {
		if (parameterIndex < 0)
			throw new IllegalArgumentException("Invalid index index: "
					+ parameterIndex);
		if (parameterName == null)
			throw new NullPointerException("Parameter name required");
		if (parameterName.isEmpty())
			throw new IllegalArgumentException(
					"Parameter name must contain characters");

		this.index = parameterIndex;
		this.parameterName = parameterName;
		this.defaultValue = defaultValue; // may be null
	}
}
