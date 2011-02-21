package uk.ac.ic.doc.gander.model;

import java.util.Map;

public interface Scope {

	public String getName();

	public String getFullName();

	public Scope getParentScope();

	public Scope lookup(String token);

	public Map<String, Class> getClasses();

	public Map<String, Function> getFunctions();

	public Map<String, Module> getModules();

	public Map<String, Package> getPackages();
}