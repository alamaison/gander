package uk.ac.ic.doc.gander.model;

import java.util.List;
import java.util.Map;

import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;

public interface Module extends Namespace {

	@Deprecated
	Map<String, Class> getClasses();

	String getFullName();

	@Deprecated
	Map<String, Function> getFunctions();

	@Deprecated
	Map<String, Module> getModules();

	String getName();

	boolean isSystem();

	boolean isTopLevel();

	@Deprecated
	Module lookup(List<String> importNameTokens);

	@Deprecated
	Member lookupMember(String memberName);

	Model model();

	ModuleCO codeObject();

	public Module getParent();

	public org.python.pydev.parser.jython.ast.Module getAst();
}