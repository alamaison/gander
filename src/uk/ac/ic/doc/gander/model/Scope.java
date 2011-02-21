package uk.ac.ic.doc.gander.model;

public interface Scope {

	public abstract String getName();
	public abstract String getFullName();
	public abstract Scope getParentScope();
	public abstract Scope lookup(String token);

}