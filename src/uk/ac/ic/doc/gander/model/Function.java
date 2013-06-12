package uk.ac.ic.doc.gander.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.flowinference.Namespace;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.FunctionCO;
import uk.ac.ic.doc.gander.model.name_binding.InScopeVariableFinder;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

public final class Function implements OldNamespace, Namespace {

	private final Map<String, Function> functions = new HashMap<String, Function>();
	private final Map<String, Class> classes = new HashMap<String, Class>();

	private final FunctionCO codeObject;
	private final Model model;

	private Cfg graph = null;

	public Function(FunctionCO codeObject, Model model) {
		this.model = model;
		this.codeObject = codeObject;
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
	 * The namespace in which a function executes is never accessible via
	 * attribute access.
	 */
	public Result<ModelSite<exprType>> references(SubgoalManager goalManager) {
		return FiniteResult.bottom();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The namespace in which a function executes is never writeable via
	 * attribute access.
	 */
	public Result<ModelSite<exprType>> writeableReferences(
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

	@Override
	public String getName() {
		return codeObject.declaredName();
	}

	@Override
	public String getFullName() {
		String parentName = getParentScope().getFullName();
		if (parentName.isEmpty())
			return getName();
		else
			return parentName + "." + getName();
	}

	@Override
	public Cfg getCfg() {
		if (graph == null)
			graph = new Cfg(getAst());
		return graph;
	}

	@Override
	public OldNamespace getParentScope() {
		CodeObject codeObjectParent = codeObject.parent();
		if (codeObjectParent != null) {
			return codeObjectParent.oldStyleConflatedNamespace();
		} else {
			return null;
		}
	}

	@Override
	@Deprecated
	public Map<String, Module> getModules() {
		return Collections.emptyMap();
	}

	@Override
	@Deprecated
	public Map<String, Class> getClasses() {
		// return Collections.unmodifiableMap(classes);
		return classes;
	}

	@Override
	@Deprecated
	public Map<String, Function> getFunctions() {
		return functions;
		// return Collections.unmodifiableMap(functions);
	}

	@Override
	@Deprecated
	public void addModule(Module pkg) {
		throw new Error("A function cannot contain a module");
	}

	@Override
	@Deprecated
	public void addClass(Class klass) {
		classes.put(klass.getName(), klass);
	}

	@Override
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
	@Override
	public boolean isSystem() {
		return getParentScope().isSystem();
	}

	@Override
	public FunctionDef getAst() {
		return codeObject.ast();
	}

	@Override
	@Deprecated
	public Member lookupMember(String memberName) {
		if (classes.containsKey(memberName))
			return classes.get(memberName);
		else if (functions.containsKey(memberName))
			return functions.get(memberName);

		return null;
	}

	@Override
	public CodeBlock asCodeBlock() {
		return codeObject.codeBlock();
	}

	@Override
	public Module getGlobalNamespace() {
		return getParentScope().getGlobalNamespace();
	}

	@Override
	public Model model() {
		return model;
	}

	@Override
	public FunctionCO codeObject() {
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
		Function other = (Function) obj;
		if (codeObject == null) {
			if (other.codeObject != null)
				return false;
		} else if (!codeObject.equals(other.codeObject))
			return false;
		return true;
	}

}
