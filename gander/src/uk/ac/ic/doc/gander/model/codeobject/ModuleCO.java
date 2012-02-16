package uk.ac.ic.doc.gander.model.codeobject;

import java.util.Collections;

import org.python.pydev.parser.jython.ast.VisitorIF;
import org.python.pydev.parser.jython.ast.exprType;
import org.python.pydev.parser.jython.ast.stmtType;

import uk.ac.ic.doc.gander.flowinference.Namespace;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.ModuleNamespace;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;
import uk.ac.ic.doc.gander.model.codeblock.DefaultCodeBlock;
import uk.ac.ic.doc.gander.model.codeblock.DefaultCodeBlock.Acceptor;

/**
 * Model of Python modules as first-class objects.
 */
public final class ModuleCO implements NamedCodeObject {

	private final String name;
	private final org.python.pydev.parser.jython.ast.Module ast;
	private ModuleNamespace oldYukkyNamespace = null;
	private CodeBlock codeBlock = null;

	/**
	 * Create new module code object representation.
	 * 
	 * @param ast
	 *            the code of the module as an abstract syntax tree
	 */
	public ModuleCO(String name, org.python.pydev.parser.jython.ast.Module ast) {
		if (name == null)
			throw new NullPointerException("All modules have names");
		// if (name.isEmpty())
		// throw new IllegalArgumentException("A module name cannot be empty");
		if (ast == null)
			throw new NullPointerException(
					"Code objects must have code associated with them");

		this.name = name;
		this.ast = ast;
	}

	@Override
	public org.python.pydev.parser.jython.ast.Module ast() {
		return ast;
	}

	@Override
	public CodeBlock codeBlock() {
		if (codeBlock == null) {

			Acceptor acceptor = new Acceptor() {

				@Override
				public void accept(VisitorIF visitor) throws Exception {
					for (stmtType stmt : ast.body) {
						stmt.accept(visitor);
					}
				}
			};

			codeBlock = new DefaultCodeBlock(
					Collections.<ModelSite<exprType>> emptyList(), acceptor);
		}

		return codeBlock;
	}

	@Override
	public ModuleCO enclosingModule() {
		return this;
	}

	@Override
	public NestedCodeObjects nestedCodeObjects() {
		return new DefaultNestedCodeObjects(this, model());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Modules are the root of any lexical binding. Any variables not already
	 * bound must bind in the module (global) namespace (or the builtins but
	 * that is a runtime decision).
	 */
	@Override
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
	@Override
	public boolean nestedVariablesCanBindHere() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Both qualified and unqualified references of a module access the same
	 * namespace.
	 */
	@Override
	@Deprecated
	public Namespace fullyQualifiedNamespace() {
		return oldStyleConflatedNamespace();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Both qualified and unqualified references of a module access the same
	 * namespace.
	 */
	@Override
	@Deprecated
	public Namespace unqualifiedNamespace() {
		return oldStyleConflatedNamespace();
	}

	@Override
	@Deprecated
	public Model model() {
		return oldStyleConflatedNamespace().model();
	}

	@Override
	@Deprecated
	public ModuleNamespace oldStyleConflatedNamespace() {
		assert oldYukkyNamespace != null;
		return oldYukkyNamespace;
	}

	@Override
	public String declaredName() {
		return name;
	}

	@Override
	public String absoluteDescription() {
		return declaredName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ast == null) ? 0 : ast.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (ast == null) {
			if (other.ast != null)
				return false;
		} else if (!ast.equals(other.ast))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ModuleCO[" + absoluteDescription() + "]";
	}

	public void setNamespace(ModuleNamespace module) {
		assert module != null;
		oldYukkyNamespace = module;
	}

	@Override
	public boolean isBuiltin() {
		return name.isEmpty();
	}

}
