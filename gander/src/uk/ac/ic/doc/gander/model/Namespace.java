package uk.ac.ic.doc.gander.model;

import java.util.Map;

import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

public interface Namespace extends Member {

	public String getFullName();

	public Member lookupMember(String memberName);

	public Map<String, Class> getClasses();

	public Map<String, Function> getFunctions();

	public Map<String, Module> getModules();

	public void addModule(Module module);

	public void addFunction(Function function);

	public void addClass(Class klass);

	public boolean isSystem();

	public Cfg getCfg();

	@Deprecated
	public CodeBlock asCodeBlock();

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