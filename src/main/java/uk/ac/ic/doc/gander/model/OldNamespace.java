package uk.ac.ic.doc.gander.model;

import java.util.Map;

import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

@Deprecated
public interface OldNamespace extends Member {

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