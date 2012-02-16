package uk.ac.ic.doc.gander.model;

import java.util.List;

import uk.ac.ic.doc.gander.importing.ImportPath;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;

public interface Model {

	ModuleCO lookup(ImportPath path);

	public abstract Module getTopLevel();

	@Deprecated
	public abstract Module lookup(String importName);

	@Deprecated
	public abstract Module lookup(List<String> importNameTokens);

}