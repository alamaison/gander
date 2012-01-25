package uk.ac.ic.doc.gander.model.codeobject;

import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.model.ModelSite;

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
	public ModelSite<Name> parameterSite() {
		return parameter;
	}
}
