package uk.ac.ic.doc.gander.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.CodeObjectDefinitionPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowGoal;
import uk.ac.ic.doc.gander.flowinference.flowgoals.InstanceCreationPosition;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.FunctionCO;
import uk.ac.ic.doc.gander.model.name_binding.InScopeVariableFinder;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

public final class Class implements Namespace {

	private final Map<String, Function> methods = new HashMap<String, Function>();
	private final Map<String, Class> classes = new HashMap<String, Class>();

	private final ClassCO codeObject;
	private final Model model;

	public Class(ClassCO codeObject, Model model) {
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
	 * A class's execution namespace is readable by attribute access on any
	 * expression that the class code object can reach as well as any expression
	 * that can result in an instance of the class.
	 */
	public Result<ModelSite<exprType>> references(SubgoalManager goalManager) {

		RedundancyEliminator<ModelSite<exprType>> references = new RedundancyEliminator<ModelSite<exprType>>();

		references.add(goalManager.registerSubgoal(new FlowGoal(
				new CodeObjectDefinitionPosition(codeObject))));
		references.add(goalManager.registerSubgoal(new FlowGoal(
				new InstanceCreationPosition(codeObject))));

		return references.result();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Only attribute accesses on the class object itself can modify the class
	 * object's namespace.
	 */
	public Result<ModelSite<exprType>> writeableReferences(
			SubgoalManager goalManager) {

		if (getParentScope().getName().isEmpty()) {
			// builtin types do not have a writable namespace
			return FiniteResult.bottom();
		} else {

			return goalManager.registerSubgoal(new FlowGoal(
					new CodeObjectDefinitionPosition(codeObject)));
		}
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

	public exprType[] inheritsFrom() {
		return codeObject.ast().bases;
	}

	public String getName() {
		return codeObject.declaredName();
	}

	public String getFullName() {
		String parentName = getParentScope().getFullName();
		if (parentName.isEmpty())
			return getName();
		else
			return parentName + "." + getName();
	}

	public Namespace getParentScope() {
		CodeObject codeObjectParent = codeObject.parent();
		if (codeObjectParent != null) {
			return codeObjectParent.oldStyleConflatedNamespace();
		} else {
			return null;
		}
	}

	@Deprecated
	public Map<String, Module> getModules() {
		return Collections.emptyMap();
	}

	@Deprecated
	public Map<String, Class> getClasses() {
		return classes;
		// return Collections.unmodifiableMap(classes);
	}

	@Deprecated
	public Map<String, Function> getFunctions() {
		return methods;
		// return Collections.unmodifiableMap(methods);
	}

	@Deprecated
	public void addModule(Module module) {
		throw new Error("A class cannot contain a package");
	}

	@Deprecated
	public void addClass(Class klass) {
		classes.put(klass.getName(), klass);
	}

	@Deprecated
	public void addFunction(Function function) {
		methods.put(function.getName(), function);
	}

	@Override
	public String toString() {
		return "Class[" + getFullName() + "]";
	}

	/**
	 * Classes inherit their systemness from their parent.
	 * 
	 * It isn't possible for a class to be system if it's containing module
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
		return getParentScope().isSystem();
	}

	public ClassDef getAst() {
		return codeObject.ast();
	}

	public Cfg getCfg() {
		throw new Error("Not implemented yet");
	}

	@Deprecated
	public Member lookupMember(String memberName) {
		if (classes.containsKey(memberName))
			return classes.get(memberName);
		else if (methods.containsKey(memberName))
			return methods.get(memberName);

		return null;
	}

	public CodeBlock asCodeBlock() {
		return codeObject.codeBlock();
	}

	public Module getGlobalNamespace() {
		return getParentScope().getGlobalNamespace();
	}

	public Model model() {
		return model;
	}

	public ClassCO codeObject() {
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
		Class other = (Class) obj;
		if (codeObject == null) {
			if (other.codeObject != null)
				return false;
		} else if (!codeObject.equals(other.codeObject))
			return false;
		return true;
	}

}
