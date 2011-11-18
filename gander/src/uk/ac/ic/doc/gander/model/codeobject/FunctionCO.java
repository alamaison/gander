package uk.ac.ic.doc.gander.model.codeobject;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.NameTok;

import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;

/**
 * Model of Python functions as first-class objects.
 * 
 * Currently just an adapter around the hopelessly conflated {@link Namespace}.
 */
public final class FunctionCO implements NamedCodeObject, NestedCodeObject {

	private final FunctionDef ast;
	private final Function oldStyleFunctionNamespace;
	private final CodeObject parent;

	/**
	 * Create new function code object representation.
	 * 
	 * @param oldStyleFunctionNamespace
	 *            the old-style namespace for the function; XXX: Eventually this
	 *            should be replaced to just take the AST
	 */
	public FunctionCO(Function oldStyleFunctionNamespace, CodeObject parent) {
		if (oldStyleFunctionNamespace == null) {
			throw new NullPointerException();
		}
		if (parent == null) {
			throw new NullPointerException(
					"All functions appear inside another code object");
		}
		/*
		if (parent.ast().equals(oldStyleFunctionNamespace.getAst())) {
			throw new IllegalArgumentException(
					"Code block cannot be its own parent");
		}
		*/

		this.oldStyleFunctionNamespace = oldStyleFunctionNamespace;
		this.parent = parent;
		this.ast = oldStyleFunctionNamespace.getAst();
	}

	public SimpleNode ast() {
		return ast;
	}

	public CodeBlock codeBlock() {
		return oldStyleFunctionNamespace.asCodeBlock();
	}

	public ModuleCO enclosingModule() {
		return parent().enclosingModule();
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
	 * When a variable is known not to bind in this function's namespace, the
	 * next code object that should be considered is the next code object that
	 * allows nested code object's variables to bind in it. I.e. not classes.
	 */
	public CodeObject lexicallyNextCodeObject() {
		if (parent().nestedVariablesCanBindHere())
			return parent;
		else
			return parent.lexicallyNextCodeObject();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Variables appearing in nested classes and functions within a function 
	 * can bind in this function's namespace if the weren't define in a 
	 * namespace between this function and their nested location.  In other 
	 * words, yes.
	 */
	public boolean nestedVariablesCanBindHere() {
		return true;
	}

	public Model model() {
		return oldStyleFunctionNamespace.model();
	}

	public String declaredName() {
		return ((NameTok) ast.name).id;
	}

	public CodeObject parent() {
		return parent;
	}

	public Namespace oldStyleConflatedNamespace() {
		return oldStyleFunctionNamespace;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ast == null) ? 0 : ast.hashCode());
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
		FunctionCO other = (FunctionCO) obj;
		if (ast == null) {
			if (other.ast != null)
				return false;
		} else if (!ast.equals(other.ast))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FunctionCO[" + declaredName() + "]";
	}

}
