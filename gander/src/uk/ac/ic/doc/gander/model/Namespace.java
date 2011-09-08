package uk.ac.ic.doc.gander.model;

import java.util.Map;

import org.python.pydev.parser.jython.SimpleNode;

import uk.ac.ic.doc.gander.cfg.Cfg;

public interface Namespace {

	public String getName();

	public String getFullName();

	public Namespace getParentScope();

	public Map<String, Class> getClasses();

	public Map<String, Function> getFunctions();

	public Map<String, Module> getModules();

	public void addModule(Module module);

	public void addFunction(Function function);

	public void addClass(Class klass);

	public boolean isSystem();

	public SimpleNode getAst();

	public Cfg getCfg();
}