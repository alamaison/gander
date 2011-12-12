package uk.ac.ic.doc.gander.model.codeobject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.NameTok;

import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Member;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;

/**
 * Model of Python functions as first-class objects.
 * 
 * Currently just an adapter around the hopelessly conflated {@link Namespace}.
 */
public final class FunctionCO implements NamedCodeObject, NestedCodeObject,
		CallableCodeObject {

	private final FunctionDef ast;
	private final Function oldStyleFunctionNamespace;
	private final CodeObject parent;

	/**
	 * Create new function code object representation.
	 * 
	 * XXX: Eventually this should be replaced to just take the AST
	 * 
	 * @param oldStyleFunctionNamespace
	 *            the old-style namespace for the function
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
		 * if (parent.ast().equals(oldStyleFunctionNamespace.getAst())) { throw
		 * new IllegalArgumentException( "Code block cannot be its own parent");
		 * }
		 */

		this.oldStyleFunctionNamespace = oldStyleFunctionNamespace;
		this.parent = parent;
		this.ast = oldStyleFunctionNamespace.getAst();
	}

	public FunctionDef ast() {
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
	 * Variables appearing in nested classes and functions within a function can
	 * bind in this function's namespace if the weren't define in a namespace
	 * between this function and their nested location. In other words, yes.
	 */
	public boolean nestedVariablesCanBindHere() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Qualified references on a function object access a separate namespace
	 * from the function body.
	 */
	public Namespace fullyQualifiedNamespace() {
		return new DummyNamespace();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Unqualified references (variables) in a function body are separate from
	 * the references on the function object.
	 */
	public Namespace unqualifiedNamespace() {
		return oldStyleFunctionNamespace;
	}

	public Model model() {
		return oldStyleFunctionNamespace.model();
	}

	public String declaredName() {
		return ((NameTok) ast.name).id;
	}

	public String absoluteDescription() {
		return parent.absoluteDescription() + "/" + declaredName();
	}

	public CodeObject parent() {
		return parent;
	}

	public Function oldStyleConflatedNamespace() {
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
		return "FunctionCO[" + absoluteDescription() + "]";
	}

}

final class DummyNamespace implements Namespace {

	private static final String ERROR = "External function namespaces are currently non-functional";

	public Namespace getParentScope() {
		throw new UnsupportedOperationException(ERROR);
	}

	public String getName() {
		throw new UnsupportedOperationException(ERROR);
	}

	public SimpleNode getAst() {
		throw new UnsupportedOperationException(ERROR);
	}

	public Model model() {
		throw new UnsupportedOperationException(ERROR);
	}

	public Member lookupMember(String memberName) {
		throw new UnsupportedOperationException(ERROR);
	}

	public boolean isSystem() {
		throw new UnsupportedOperationException(ERROR);
	}

	public Map<String, Module> getModules() {
		throw new UnsupportedOperationException(ERROR);
	}

	public Module getGlobalNamespace() {
		throw new UnsupportedOperationException(ERROR);
	}

	public Map<String, Function> getFunctions() {
		throw new UnsupportedOperationException(ERROR);
	}

	public String getFullName() {
		throw new UnsupportedOperationException(ERROR);
	}

	public Map<String, Class> getClasses() {
		throw new UnsupportedOperationException(ERROR);
	}

	public Cfg getCfg() {
		throw new UnsupportedOperationException(ERROR);
	}

	public CodeObject codeObject() {
		throw new UnsupportedOperationException(ERROR);
	}

	public CodeBlock asCodeBlock() {
		throw new UnsupportedOperationException(ERROR);
	}

	public void addModule(Module module) {
		throw new UnsupportedOperationException(ERROR);
	}

	public void addFunction(Function function) {
		throw new UnsupportedOperationException(ERROR);
	}

	public void addClass(Class klass) {
		throw new UnsupportedOperationException(ERROR);
	}
}
