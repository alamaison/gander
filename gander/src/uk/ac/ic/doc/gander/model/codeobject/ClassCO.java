package uk.ac.ic.doc.gander.model.codeobject;

import java.util.HashSet;
import java.util.Set;

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
public final class ClassCO implements NamedCodeObject, NestedCodeObject,
		CallableCodeObject {

	private final ClassDef ast;
	private final Class oldStyleClassNamespace;
	private final CodeObject parent;

	/**
	 * Create new function code object representation.
	 * 
	 * @param oldStyleClassNamespace
	 *            the old-style namespace for the function; XXX: Eventually this
	 *            should be replaced to just take the AST
	 */
	public ClassCO(Class oldStyleClassNamespace, CodeObject parent) {
		if (oldStyleClassNamespace == null) {
			throw new NullPointerException();
		}
		if (parent == null) {
			throw new NullPointerException(
					"All classes appear inside another code object");
		}
		/*
		 * if (parent.ast().equals(oldStyleClassNamespace.getAst())) { throw new
		 * IllegalArgumentException( "Code block cannot be its own parent"); }
		 */

		this.oldStyleClassNamespace = oldStyleClassNamespace;
		this.parent = parent;
		this.ast = oldStyleClassNamespace.getAst();
	}

	public ClassDef ast() {
		return ast;
	}

	public CodeBlock codeBlock() {
		return oldStyleClassNamespace.asCodeBlock();
	}

	public ModuleCO enclosingModule() {
		return parent().enclosingModule();
	}

	public Set<CodeObject> nestedCodeObjects() {
		Set<CodeObject> nestedCodeObjects = new HashSet<CodeObject>();
		for (Namespace namespace : oldStyleClassNamespace.getModules().values()) {
			nestedCodeObjects.add(namespace.codeObject());
		}
		for (Namespace namespace : oldStyleClassNamespace.getClasses().values()) {
			nestedCodeObjects.add(namespace.codeObject());
		}
		for (Namespace namespace : oldStyleClassNamespace.getFunctions()
				.values()) {
			nestedCodeObjects.add(namespace.codeObject());
		}
		return nestedCodeObjects;
	}

	public Model model() {
		return oldStyleClassNamespace.model();
	}

	public String declaredName() {
		return ((NameTok) ast.name).id;
	}

	public String absoluteDescription() {
		return parent().absoluteDescription() + "/" + declaredName();
	}

	public CodeObject parent() {
		return parent;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * When a variable is known not to bind in this class's namespace, the next
	 * code object that should be considered is the next code object that allows
	 * nested code object's variables to bind in it. I.e. not classes.
	 */
	public CodeObject lexicallyNextCodeObject() {
		if (parent().nestedVariablesCanBindHere())
			return parent();
		else
			return parent().lexicallyNextCodeObject();
	}

	public boolean nestedVariablesCanBindHere() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Both qualified and unqualified references of a class access the same
	 * namespace.
	 */
	public Namespace fullyQualifiedNamespace() {
		return oldStyleClassNamespace;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Both qualified and unqualified references of a class access the same
	 * namespace.
	 */
	public Namespace unqualifiedNamespace() {
		return oldStyleClassNamespace;
	}

	public Class oldStyleConflatedNamespace() {
		return oldStyleClassNamespace;
	}

	public FormalParameters formalParameters() {
		return FormalParameters.EMPTY_PARAMETERS;
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
		return "ClassCO[" + absoluteDescription() + "]";
	}

}
