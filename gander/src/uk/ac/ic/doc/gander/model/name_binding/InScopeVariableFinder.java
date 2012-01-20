package uk.ac.ic.doc.gander.model.name_binding;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

/**
 * Finds variables that are in the scope of the given code object with the given
 * name.
 */
public final class InScopeVariableFinder {

	private final Set<Variable> variables = new HashSet<Variable>();
	private final String variableName;
	private final NamespaceName namespaceName;

	public InScopeVariableFinder(CodeObject codeObject, String variableName) {
		this.variableName = variableName;

		this.namespaceName = new NamespaceName(variableName, codeObject
				.unqualifiedNamespace());

		addVariableIfInScope(codeObject);
	}

	public Set<Variable> variables() {
		return variables;
	}

	private void addVariableIfInScope(CodeObject codeObject) {

		Variable localVariable = new Variable(variableName, codeObject);

		if (new NamespaceName(localVariable.bindingLocation())
				.equals(namespaceName)) {
			variables.add(localVariable);
		}

		for (CodeObject nestedCodeObject : codeObject.nestedCodeObjects()) {
			addVariableIfInScope(nestedCodeObject);
		}
	}
}