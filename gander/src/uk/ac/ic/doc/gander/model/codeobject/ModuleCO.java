package uk.ac.ic.doc.gander.model.codeobject;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;

/**
 * Model of Python modules as first-class objects.
 * 
 * Currently just an adapter around the hopelessly conflated {@link Namespace}.
 */
public final class ModuleCO implements NamedCodeObject {

	private final Module oldStyleFunctionNamespace;

	/**
	 * Create new function code object representation.
	 * 
	 * @param oldStyleFunctionNamespace
	 *            the old-style namespace for the function; XXX: Eventually this
	 *            should be replaced to just take the AST
	 */
	public ModuleCO(Module oldStyleFunctionNamespace) {
		if (oldStyleFunctionNamespace == null)
			throw new NullPointerException();

		this.oldStyleFunctionNamespace = oldStyleFunctionNamespace;
	}

	public org.python.pydev.parser.jython.ast.Module ast() {
		return oldStyleFunctionNamespace.getAst();
	}

	public CodeBlock codeBlock() {
		return oldStyleFunctionNamespace.asCodeBlock();
	}

	public ModuleCO enclosingModule() {
		return this;
	}

	public Set<CodeObject> nestedCodeObjects() {
		Set<CodeObject> nestedCodeObjects = new HashSet<CodeObject>();
		for (Namespace namespace : oldStyleFunctionNamespace.getModules()
				.values()) {
			nestedCodeObjects.add(namespace.codeObject());
		}
		for (Namespace namespace : oldStyleFunctionNamespace.getClasses()
				.values()) {
			nestedCodeObjects.add(namespace.codeObject());
		}
		for (Namespace namespace : oldStyleFunctionNamespace.getFunctions()
				.values()) {
			nestedCodeObjects.add(namespace.codeObject());
		}
		return nestedCodeObjects;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Modules are the root of any lexical binding. Any variables not already
	 * bound must bind in the module (global) namespace (or the builtins but
	 * that is a runtime decision).
	 */
	public CodeObject lexicallyNextCodeObject() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Variables in code objects within a module can bind in the module (global)
	 * scope if the variable is not defined in a scope between there and the
	 * occurrence. In other words, yes.
	 */
	public boolean nestedVariablesCanBindHere() {
		return true;
	}

	public Model model() {
		return oldStyleFunctionNamespace.model();
	}

	public Namespace oldStyleConflatedNamespace() {
		return oldStyleFunctionNamespace;
	}

	public String declaredName() {
		return oldStyleFunctionNamespace.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((oldStyleFunctionNamespace == null) ? 0
						: oldStyleFunctionNamespace.hashCode());
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
		ModuleCO other = (ModuleCO) obj;
		if (oldStyleFunctionNamespace == null) {
			if (other.oldStyleFunctionNamespace != null)
				return false;
		} else if (!oldStyleFunctionNamespace
				.equals(other.oldStyleFunctionNamespace))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ModuleCO[" + declaredName() + "]";
	}

}
