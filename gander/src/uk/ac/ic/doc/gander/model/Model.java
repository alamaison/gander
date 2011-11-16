package uk.ac.ic.doc.gander.model;

import java.util.List;

public interface Model {

	public abstract Module getTopLevel();

	public abstract Module lookup(String importName);

	public abstract Module lookup(List<String> importNameTokens);

}