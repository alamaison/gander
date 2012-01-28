package uk.ac.ic.doc.gander.model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowGoal;
import uk.ac.ic.doc.gander.flowinference.flowgoals.InstanceCreationPosition;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

public final class ObjectInstanceNamespace implements Namespace {

	private final ClassCO classObject;

	public ObjectInstanceNamespace(ClassCO classObject) {
		this.classObject = classObject;
	}

	public Result<ModelSite<exprType>> references(
			SubgoalManager goalManager) {

		return goalManager.registerSubgoal(new FlowGoal(
				new InstanceCreationPosition(classObject)));
	}

	public Result<ModelSite<exprType>> writeableReferences(
			SubgoalManager goalManager) {

		return goalManager.registerSubgoal(new FlowGoal(
				new InstanceCreationPosition(classObject)));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Variables cannot access the value of names in a normal object's
	 * namespace. Only code-objects are affected that way. Object members must
	 * be accessed via an attribute access on the object.
	 */
	public Set<Variable> variablesInScope(String name) {
		return Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Variables cannot affect the value of names in a normal object's
	 * namespace. Only code-objects are affected that way. Object members must
	 * be set via an assignment to an attribute access on the object.
	 */
	public Set<Variable> variablesWriteableInScope(String name) {
		return Collections.emptySet();
	}

	public void addClass(Class klass) {
		throw new UnsupportedOperationException();
	}

	public void addFunction(Function function) {
		throw new UnsupportedOperationException();
	}

	public void addModule(Module module) {
		throw new UnsupportedOperationException();
	}

	public CodeBlock asCodeBlock() {
		throw new UnsupportedOperationException();
	}

	public CodeObject codeObject() {
		throw new UnsupportedOperationException();
	}

	public Cfg getCfg() {
		throw new UnsupportedOperationException();
	}

	public Map<String, Class> getClasses() {
		throw new UnsupportedOperationException();
	}

	public String getFullName() {
		throw new UnsupportedOperationException();
	}

	public Map<String, Function> getFunctions() {
		throw new UnsupportedOperationException();
	}

	public Module getGlobalNamespace() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, Module> getModules() {
		throw new UnsupportedOperationException();
	}

	public boolean isSystem() {
		throw new UnsupportedOperationException();
	}

	public Member lookupMember(String memberName) {
		throw new UnsupportedOperationException();
	}

	public Model model() {
		throw new UnsupportedOperationException();
	}

	public SimpleNode getAst() {
		throw new UnsupportedOperationException();
	}

	public String getName() {
		throw new UnsupportedOperationException();
	}

	public Namespace getParentScope() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((classObject == null) ? 0 : classObject.hashCode());
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
		ObjectInstanceNamespace other = (ObjectInstanceNamespace) obj;
		if (classObject == null) {
			if (other.classObject != null)
				return false;
		} else if (!classObject.equals(other.classObject))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ObjectInstanceNamespace [classObject=" + classObject + "]";
	}

}
