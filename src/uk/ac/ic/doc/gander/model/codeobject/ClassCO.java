package uk.ac.ic.doc.gander.model.codeobject;

import java.util.Collections;
import java.util.List;

import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.VisitorIF;
import org.python.pydev.parser.jython.ast.exprType;
import org.python.pydev.parser.jython.ast.stmtType;

import uk.ac.ic.doc.gander.flowinference.Namespace;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;
import uk.ac.ic.doc.gander.model.codeblock.DefaultCodeBlock;
import uk.ac.ic.doc.gander.model.codeblock.DefaultCodeBlock.Acceptor;
import uk.ac.ic.doc.gander.model.parameters.FormalParameters;

/**
 * Model of Python classes as first-class objects.
 */
public final class ClassCO implements NamedCodeObject, NestedCodeObject {

	private final ClassDef ast;
	private final CodeObject parent;
	private Class yukkyOldNamespace = null;
	private CodeBlock codeBlock = null;

	/**
	 * Create new class code object representation.
	 * 
	 * @param ast
	 *            the code of the class as an abstract syntax tree
	 * @param parent
	 *            the code object that this class is declared within
	 */
	ClassCO(ClassDef ast, CodeObject parent) {
		if (ast == null) {
			throw new NullPointerException(
					"Code objects must have code associated with them");
		}
		if (parent == null) {
			throw new NullPointerException(
					"Classes are always contained within another code objects");
		}
		if (parent.ast().equals(ast)) {
			throw new IllegalArgumentException(
					"Code object cannot be its own parent");
		}

		this.ast = ast;
		this.parent = parent;
	}

	@Override
	public ClassDef ast() {
		return ast;
	}

	@Override
	public CodeBlock codeBlock() {
		if (codeBlock == null) {
			// Classes don't have parameters that get bound after
			// declaration
			//
			// XXX: WTF? The ClassDef node has parameters! Are these from
			// the constructor?
			List<ModelSite<exprType>> args = Collections.emptyList();

			Acceptor acceptor = new Acceptor() {

				@Override
				public void accept(VisitorIF visitor) throws Exception {
					for (stmtType stmt : ast.body) {
						stmt.accept(visitor);
					}
				}
			};

			codeBlock = new DefaultCodeBlock(FormalParameters.EMPTY_PARAMETERS,
					acceptor);
		}

		return codeBlock;
	}

	@Override
	public ModuleCO enclosingModule() {
		return parent().enclosingModule();
	}

	@Override
	public NestedCodeObjects nestedCodeObjects() {
		return new DefaultNestedCodeObjects(this, model());
	}

	@Override
	public Model model() {
		return oldStyleConflatedNamespace().model();
	}

	@Override
	public String declaredName() {
		return ((NameTok) ast.name).id;
	}

	@Override
	public String absoluteDescription() {
		return parent().absoluteDescription() + "/" + declaredName();
	}

	@Override
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
	@Override
	public CodeObject lexicallyNextCodeObject() {
		if (parent().nestedVariablesCanBindHere())
			return parent();
		else
			return parent().lexicallyNextCodeObject();
	}

	@Override
	public boolean nestedVariablesCanBindHere() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Both qualified and unqualified references of a class access the same
	 * namespace.
	 */
	@Override
	public Namespace fullyQualifiedNamespace() {
		return oldStyleConflatedNamespace();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Both qualified and unqualified references of a class access the same
	 * namespace.
	 */
	@Override
	public Namespace unqualifiedNamespace() {
		return oldStyleConflatedNamespace();
	}

	@Override
	@Deprecated
	public Class oldStyleConflatedNamespace() {
		assert yukkyOldNamespace != null;
		return yukkyOldNamespace;
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

	public void setNamespace(Class namespace) {
		assert namespace != null;
		yukkyOldNamespace = namespace;
	}

	@Override
	public boolean isBuiltin() {
		return parent.isBuiltin();
	}

}
