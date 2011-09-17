package uk.ac.ic.doc.gander.model;

import java.util.Map;


import uk.ac.ic.doc.gander.cfg.Cfg;

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

	public CodeBlock asCodeBlock();
}