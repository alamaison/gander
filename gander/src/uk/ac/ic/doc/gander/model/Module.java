package uk.ac.ic.doc.gander.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.python.pydev.parser.jython.ast.VisitorIF;
import org.python.pydev.parser.jython.ast.exprType;
import org.python.pydev.parser.jython.ast.stmtType;

import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.CodeObjectNamespacePosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowGoal;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;
import uk.ac.ic.doc.gander.model.codeblock.DefaultCodeBlock;
import uk.ac.ic.doc.gander.model.codeblock.DefaultCodeBlock.Acceptor;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

/**
 * Model elements that have associated code that can be loaded.
 * 
 * In other words, represents Packages and Modules in the model. There are no
 * separate packages, only modules. After all, this is how Python sees things.
 * The Hierarchy would still maintain the distinction between them.
 */
public final class Module implements Namespace {

	private org.python.pydev.parser.jython.ast.Module ast;
	private final HashMap<String, Class> classes = new HashMap<String, Class>();
	private final HashMap<String, Function> functions = new HashMap<String, Function>();
	private final HashMap<String, Module> modules = new HashMap<String, Module>();

	private final boolean isSystem;
	private final String name;
	private final Module parent;
	private CodeBlock codeBlock = null;
	private final Model model;
	private final ModuleCO codeObject;

	public Module(String name, Module parent, Model model, boolean isSystem) {
		this.name = name;
		this.parent = parent;
		this.model = model;
		this.isSystem = isSystem;
		this.codeObject = new ModuleCO(this);
	}

	public Result<ModelSite<? extends exprType>> references(
			SubgoalManager goalManager) {

		return goalManager.registerSubgoal(new FlowGoal(
				new CodeObjectNamespacePosition(codeObject)));
	}

	public Set<Variable> variablesInScope(String name) {

		Set<Variable> variables = new HashSet<Variable>();
		NamespaceName namespaceName = new NamespaceName(name, this);

		addVariableIfInScope(name, namespaceName, codeObject, variables);

		for (CodeObject nestedCodeObject : codeObject.nestedCodeObjects()) {
			addVariableIfInScope(name, namespaceName, nestedCodeObject,
					variables);
		}

		return variables;
	}

	static private void addVariableIfInScope(String name,
			NamespaceName namespaceName, CodeObject codeObject,
			Set<Variable> variables) {

		Variable localVariable = new Variable(name, codeObject);

		if (localVariable.bindingLocation().equals(namespaceName)) {
			variables.add(localVariable);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * For global (module) variables, all in-scope variables are writeable.
	 */
	public Set<Variable> variablesWriteableInScope(String name) {
		return variablesInScope(name);
	}

	public void setAst(org.python.pydev.parser.jython.ast.Module ast) {
		assert ast != null;
		this.ast = ast;
	}

	public void addClass(Class subclass) {
		classes.put(subclass.getName(), subclass);
	}

	public void addFunction(Function subfunction) {
		functions.put(subfunction.getName(), subfunction);
	}

	public void addModule(Module submodule) {
		modules.put(submodule.getName(), submodule);
	}

	public org.python.pydev.parser.jython.ast.Module getAst() {
		if (ast == null)
			throw new AssertionError(
					"Trying to get the AST before it has been built");
		return ast;
	}

	public Cfg getCfg() {
		throw new Error("Not implemented yet");
	}

	public Map<String, Class> getClasses() {
		// return Collections.unmodifiableMap(classes);
		return classes;
	}

	public String getFullName() {
		if (isTopLevel())
			return getName();
		else {
			String parentName = parent.getFullName();
			if (parentName.isEmpty())
				return getName();
			else
				return parentName + "." + getName();
		}
	}

	public Map<String, Function> getFunctions() {
		// return Collections.unmodifiableMap(functions);
		return functions;
	}

	public Map<String, Module> getModules() {
		// return Collections.unmodifiableMap(modules);
		return modules;
	}

	public String getName() {
		return name;
	}

	public Module getParent() {
		return parent;
	}

	public Namespace getParentScope() {
		return getParent();
	}

	public boolean isSystem() {
		return isSystem;
	}

	public boolean isTopLevel() {
		return getParent() == null;
	}

	public Module lookup(List<String> importNameTokens) {
		Queue<String> tokens = new LinkedList<String>(importNameTokens);

		Module scope = this;
		while (scope != null && !tokens.isEmpty()) {
			String token = tokens.remove();
			if (tokens.isEmpty())
				return scope.getModules().get(token);
			else
				scope = scope.getModules().get(token);
		}

		return scope;
	}

	@Override
	public String toString() {
		return "Module[" + getFullName() + "]";
	}

	public Member lookupMember(String memberName) {
		if (modules.containsKey(memberName))
			return modules.get(memberName);
		else if (classes.containsKey(memberName))
			return classes.get(memberName);
		else if (functions.containsKey(memberName))
			return functions.get(memberName);

		return null;
	}

	public CodeBlock asCodeBlock() {
		if (codeBlock == null) {

			Acceptor acceptor = new Acceptor() {

				public void accept(VisitorIF visitor) throws Exception {
					for (stmtType stmt : ast.body) {
						stmt.accept(visitor);
					}
				}
			};

			codeBlock = new DefaultCodeBlock(Collections
					.<ModelSite<exprType>> emptyList(), acceptor);
		}

		return codeBlock;
	}

	public Module getGlobalNamespace() {
		return this;
	}

	public Model model() {
		return model;
	}

	public ModuleCO codeObject() {
		return codeObject;
	}
}
