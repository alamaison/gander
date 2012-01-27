package uk.ac.ic.doc.gander.model.codeobject;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.VisitorIF;
import org.python.pydev.parser.jython.ast.argumentsType;
import org.python.pydev.parser.jython.ast.exprType;
import org.python.pydev.parser.jython.ast.stmtType;

import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.CodeObjectDefinitionPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowGoal;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Member;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;
import uk.ac.ic.doc.gander.model.codeblock.DefaultCodeBlock;
import uk.ac.ic.doc.gander.model.codeblock.DefaultCodeBlock.Acceptor;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

/**
 * Model of Python functions as first-class objects.
 */
public final class FunctionCO implements NamedCodeObject, NestedCodeObject,
		InvokableCodeObject {

	private final FunctionDef ast;
	private final CodeObject parent;
	private Function yukkyOldNamespace = null;
	private CodeBlock codeBlock = null;

	/**
	 * Create new function code object representation.
	 * 
	 * @param ast
	 *            the code of the function as an abstract syntax tree
	 * @param parent
	 *            the code object that this function is declared within
	 */
	public FunctionCO(FunctionDef ast, CodeObject parent) {
		if (ast == null) {
			throw new NullPointerException(
					"Code objects must have code associated with them");
		}
		if (parent == null) {
			throw new NullPointerException(
					"Functions are always contained within another code objects");
		}
		if (parent.ast().equals(ast)) {
			throw new IllegalArgumentException(
					"Code object cannot be its own parent");
		}

		this.ast = ast;
		this.parent = parent;
	}

	public FunctionDef ast() {
		return ast;
	}

	public CodeBlock codeBlock() {
		if (codeBlock == null) {

			Acceptor acceptor = new Acceptor() {

				public void accept(VisitorIF visitor) throws Exception {
					ast.args.accept(visitor);

					for (stmtType stmt : ast.body) {
						stmt.accept(visitor);
					}
				}
			};

			codeBlock = new DefaultCodeBlock(formalParameters().parameters(),
					acceptor);
		}

		return codeBlock;
	}

	public ModuleCO enclosingModule() {
		return parent().enclosingModule();
	}

	public NestedCodeObjects nestedCodeObjects() {
		return new DefaultNestedCodeObjects(this, model());
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
		return new FunctionObjectNamespace(this);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Unqualified references (variables) in a function body are separate from
	 * the references on the function object.
	 */
	public Namespace unqualifiedNamespace() {
		return oldStyleConflatedNamespace();
	}

	public Model model() {
		return oldStyleConflatedNamespace().model();
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

	@Deprecated
	public Function oldStyleConflatedNamespace() {
		assert yukkyOldNamespace != null;
		return yukkyOldNamespace;
	}

	public FormalParameters formalParameters() {
		return new FormalParameters(new ModelSite<argumentsType>(ast().args,
				this));
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

	public void setNamespace(Function function) {
		assert function != null;
		yukkyOldNamespace = function;
	}

}

final class FunctionObjectNamespace implements Namespace {

	private final FunctionCO codeObject;

	FunctionObjectNamespace(FunctionCO codeObject) {
		this.codeObject = codeObject;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * A function object's namespace is accessible by attribute reference from
	 * anywhere the function object flows to.
	 */
	public Result<ModelSite<exprType>> references(SubgoalManager goalManager) {
		return goalManager.registerSubgoal(new FlowGoal(
				new CodeObjectDefinitionPosition(codeObject)));
	}

	public Set<Variable> variablesInScope(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<Variable> variablesWriteableInScope(String name) {
		return Collections.emptySet();
	}

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
		return codeObject;
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
