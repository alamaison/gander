package uk.ac.ic.doc.gander.model.codeobject;

import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.NameTok;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;

/**
 * Model of Python classes as first-class objects.
 * 
 * Currently just an adapter around the hopelessly conflated {@link Namespace}.
 */
public final class ClassCO implements NamedCodeObject, NestedCodeObject {

	private final ClassDef ast;
	private final Class oldStyleFunctionNamespace;
	private final CodeObject parent;

	/**
	 * Create new function code object representation.
	 * 
	 * @param oldStyleFunctionNamespace
	 *            the old-style namespace for the function; XXX: Eventually this
	 *            should be replaced to just take the AST
	 */
	public ClassCO(Class oldStyleFunctionNamespace, CodeObject parent) {
		if (oldStyleFunctionNamespace == null) {
			throw new NullPointerException();
		}
		if (parent == null) {
			throw new NullPointerException(
					"All classes appear inside another code object");
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

	public ClassDef ast() {
		return ast;
	}

	public CodeBlock codeBlock() {
		return oldStyleFunctionNamespace.asCodeBlock();
	}

	public Model model() {
		return oldStyleFunctionNamespace.model();
	}

	public String declaredName() {
		return ((NameTok) ast.name).id;
	}

	public CodeObject enclosingCodeObject() {
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
		ClassCO other = (ClassCO) obj;
		if (ast == null) {
			if (other.ast != null)
				return false;
		} else if (!ast.equals(other.ast))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ClassCO [ast=" + ast + "]";
	}

}
