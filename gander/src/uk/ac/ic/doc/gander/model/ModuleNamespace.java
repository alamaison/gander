package uk.ac.ic.doc.gander.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.CodeObjectDefinitionPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowGoal;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.FunctionCO;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;
import uk.ac.ic.doc.gander.model.name_binding.InScopeVariableFinder;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

/**
 * Model elements that have associated code that can be loaded.
 * 
 * In other words, represents Packages and Modules in the model. There are no
 * separate packages, only modules. After all, this is how Python sees things.
 * The Hierarchy would still maintain the distinction between them.
 */
public final class ModuleNamespace implements Module {

	private final HashMap<String, Class> classes = new HashMap<String, Class>();
	private final HashMap<String, Function> functions = new HashMap<String, Function>();
	private final HashMap<String, Module> modules = new HashMap<String, Module>();

	private final ModuleCO codeObject;
	private final boolean isSystem;
	private final Module parent;
	private final Model model;

	public ModuleNamespace(ModuleCO codeObject, Module parent, Model model,
			boolean isSystem) {
		if (codeObject == null)
			throw new NullPointerException("Code object not optional");
		if (model == null)
			throw new NullPointerException("Model not optional");

		this.codeObject = codeObject;
		this.parent = parent;
		this.model = model;
		this.isSystem = isSystem;
	}

	public void addNestedCodeObjects() {
		for (CodeObject nestedCodeObject : codeObject.nestedCodeObjects()) {
			if (nestedCodeObject instanceof ClassCO) {
				addClass(((ClassCO) nestedCodeObject)
						.oldStyleConflatedNamespace());
			} else if (nestedCodeObject instanceof FunctionCO) {
				addFunction(((FunctionCO) nestedCodeObject)
						.oldStyleConflatedNamespace());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * A module's execution namespace is accessible by attribute access on any
	 * expression that the module code object can reach.
	 */
	public Result<ModelSite<? extends exprType>> references(
			SubgoalManager goalManager) {

		return goalManager.registerSubgoal(new FlowGoal(
				new CodeObjectDefinitionPosition(codeObject)));
	}

	public Set<Variable> variablesInScope(String name) {
		return new InScopeVariableFinder(codeObject, name).variables();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * For global (module) variables, all in-scope variables are writeable.
	 */
	public Set<Variable> variablesWriteableInScope(String name) {
		return variablesInScope(name);
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
		return codeObject.ast();
	}

	public Cfg getCfg() {
		throw new Error("Not implemented yet");
	}

	@Deprecated
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

	@Deprecated
	public Map<String, Function> getFunctions() {
		// return Collections.unmodifiableMap(functions);
		return functions;
	}

	@Deprecated
	public Map<String, Module> getModules() {
		// return Collections.unmodifiableMap(modules);
		return modules;
	}

	public String getName() {
		return codeObject.declaredName();
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

	@Deprecated
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
		return "ModuleNamespace[" + getFullName() + "]";
	}

	@Deprecated
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
		return codeObject.codeBlock();
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((codeObject == null) ? 0 : codeObject.hashCode());
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
		ModuleNamespace other = (ModuleNamespace) obj;
		if (codeObject == null) {
			if (other.codeObject != null)
				return false;
		} else if (!codeObject.equals(other.codeObject))
			return false;
		return true;
	}

}
