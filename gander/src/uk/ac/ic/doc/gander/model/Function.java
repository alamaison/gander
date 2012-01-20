package uk.ac.ic.doc.gander.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.VisitorIF;
import org.python.pydev.parser.jython.ast.exprType;
import org.python.pydev.parser.jython.ast.stmtType;

import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;
import uk.ac.ic.doc.gander.model.codeblock.DefaultCodeBlock;
import uk.ac.ic.doc.gander.model.codeblock.DefaultCodeBlock.Acceptor;
import uk.ac.ic.doc.gander.model.codeobject.FunctionCO;
import uk.ac.ic.doc.gander.model.name_binding.InScopeVariableFinder;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

public final class Function implements Namespace {

	private final FunctionCO codeObject;
	private final FunctionDef function;
	private final Namespace parent;
	private final Model model;
	private final Map<String, Function> functions = new HashMap<String, Function>();
	private final Map<String, Class> classes = new HashMap<String, Class>();

	private CodeBlock codeBlock = null;
	private Cfg graph = null;

	public Function(FunctionDef function, Namespace parent, Model model) {
		this.function = function;
		this.parent = parent;
		this.model = model;
		this.codeObject = new FunctionCO(this, parent.codeObject());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The namespace in which a function executes is never accessible via
	 * attribute access.
	 */
	public Result<ModelSite<? extends exprType>> references(
			SubgoalManager goalManager) {
		return FiniteResult.bottom();
	}

	public Set<Variable> variablesInScope(String name) {
		return new InScopeVariableFinder(codeObject, name).variables();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Legally, only global names (i.e. ones whose binding location is their
	 * containing module) can be written to inside a nested code object so this
	 * will return at most one variable.
	 */
	public Set<Variable> variablesWriteableInScope(String name) {

		Variable localVariable = new Variable(name, codeObject);

		if (localVariable.bindingLocation().codeObject().equals(codeObject)) {
			return Collections.singleton(localVariable);
		} else {
			return Collections.emptySet();
		}
	}

	public String getName() {
		return ((NameTok) (function.name)).id;
	}

	public String getFullName() {
		String parentName = parent.getFullName();
		if (parentName.isEmpty())
			return getName();
		else
			return parentName + "." + getName();
	}

	public Cfg getCfg() {
		if (graph == null)
			graph = new Cfg(function);
		return graph;
	}

	public Namespace getParentScope() {
		return parent;
	}

	@Deprecated
	public Map<String, Module> getModules() {
		return Collections.emptyMap();
	}

	@Deprecated
	public Map<String, Class> getClasses() {
		// return Collections.unmodifiableMap(classes);
		return classes;
	}

	@Deprecated
	public Map<String, Function> getFunctions() {
		return functions;
		// return Collections.unmodifiableMap(functions);
	}

	@Deprecated
	public void addModule(Module pkg) {
		throw new Error("A function cannot contain a module");
	}

	@Deprecated
	public void addClass(Class klass) {
		classes.put(klass.getName(), klass);
	}

	@Deprecated
	public void addFunction(Function function) {
		functions.put(function.getName(), function);
	}

	@Override
	public String toString() {
		return "Function[" + getFullName() + "]";
	}

	/**
	 * Functions inherit their systemness from their parent.
	 * 
	 * It isn't possible for a function to be system if it's containing module
	 * isn't a system module. In other words, the resolution of systemness is at
	 * the module level and all namespaces below that, inherit from their
	 * parent.
	 * 
	 * XXX: Another way to look at this is that systemness is a property of the
	 * associated <b>hierarchy</b> element so perhaps we should link model
	 * element to their hierarchy parent. However, some model elements don't
	 * have a hierarchy element. For example the dummy_builtin module.
	 */
	public boolean isSystem() {
		return parent.isSystem();
	}

	public FunctionDef getAst() {
		return function;
	}

	@Deprecated
	public Member lookupMember(String memberName) {
		if (classes.containsKey(memberName))
			return classes.get(memberName);
		else if (functions.containsKey(memberName))
			return functions.get(memberName);

		return null;
	}

	public CodeBlock asCodeBlock() {
		if (codeBlock == null) {

			Acceptor acceptor = new Acceptor() {

				public void accept(VisitorIF visitor) throws Exception {
					function.args.accept(visitor);

					for (stmtType stmt : function.body) {
						stmt.accept(visitor);
					}
				}
			};

			codeBlock = new DefaultCodeBlock(codeObject.formalParameters()
					.parameters(), acceptor);
		}

		return codeBlock;
	}

	public Module getGlobalNamespace() {
		return getParentScope().getGlobalNamespace();
	}

	public Model model() {
		return model;
	}

	public FunctionCO codeObject() {
		return codeObject;
	}
}
