package uk.ac.ic.doc.gander.model;

import java.util.Map;
import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

public interface Namespace extends Member {

	/**
	 * Returns the expressions that expose this namespace.
	 * 
	 * In other words, the expressions that may hold an object that, via an
	 * attribute access, can read values from names of this namespace.
	 * 
	 * There is no such thing as a write-only namespace so this is a superset of
	 * the writeable references.
	 * 
	 * @param goalManager
	 *            allows us to use type inference to determine the result.
	 */
	public Result<ModelSite<exprType>> references(SubgoalManager goalManager);

	/**
	 * Returns the expressions that expose this namespace such that its members
	 * may be modified.
	 * 
	 * In other words, the expressions that may hold an object that, via a
	 * binding to attribute access, can set names of this namespace.
	 * 
	 * @param goalManager
	 *            allows us to use type inference to determine the result.
	 */
	public Result<ModelSite<exprType>> writeableReferences(
			SubgoalManager goalManager);

	/**
	 * Returns the set of variables that can read the value of the given name in
	 * this namespace.
	 */
	public Set<Variable> variablesInScope(String name);

	/**
	 * Returns the set of variables that can set the value of the given name in
	 * this namespace.
	 */
	public Set<Variable> variablesWriteableInScope(String name);

	public String getFullName();

	@Deprecated
	public Member lookupMember(String memberName);

	@Deprecated
	public Map<String, Class> getClasses();

	@Deprecated
	public Map<String, Function> getFunctions();

	@Deprecated
	public Map<String, Module> getModules();

	@Deprecated
	public void addModule(Module module);

	@Deprecated
	public void addFunction(Function function);

	@Deprecated
	public void addClass(Class klass);

	public boolean isSystem();

	public Cfg getCfg();

	@Deprecated
	public CodeBlock asCodeBlock();

	@Deprecated
	public CodeObject codeObject();

	/**
	 * Returns the Python global namespace for the model item.
	 * 
	 * Python defined this as the module in which the item appears or, if the
	 * item is a module, the module itself.
	 * 
	 * @return the {@link Module} enclosing this item.
	 */
	public Module getGlobalNamespace();

	public Model model();
}