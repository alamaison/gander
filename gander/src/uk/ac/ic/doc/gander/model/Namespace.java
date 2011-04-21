package uk.ac.ic.doc.gander.model;

import java.util.Map;

import org.python.pydev.parser.jython.SimpleNode;

public interface Namespace {

	public String getName();

	public String getFullName();

	public Namespace getParentScope();

	public Map<String, Class> getClasses();

	public Map<String, Function> getFunctions();

	public Map<String, Module> getModules();

	public Map<String, Package> getPackages();

	public void addPackage(Package pkg);

	public void addModule(Module module);

	public void addFunction(Function function);

	public void addClass(Class klass);
	
	public boolean isSystem();

	public SimpleNode getAst();
}