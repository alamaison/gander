package uk.ac.ic.doc.gander.model;

import java.util.Map;

public interface Namespace {

	public String getName();

	public String getFullName();

	public Namespace getParentScope();

	public Map<String, Class> getClasses();

	public Map<String, Function> getFunctions();

	public Map<String, Module> getModules();

	public Map<String, Package> getPackages();
}