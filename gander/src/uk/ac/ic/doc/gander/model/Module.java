package uk.ac.ic.doc.gander.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import uk.ac.ic.doc.gander.cfg.Cfg;

/**
 * Model elements that have associated code that can be loaded.
 * 
 * In other words, represents Packages and Modules in the model. There are no
 * separate packages, only modules. After all, this is how Python sees things.
 * The Hierarchy would still maintain the distinction between them.
 */
public class Module implements Namespace {

	private org.python.pydev.parser.jython.ast.Module ast;
	private HashMap<String, Class> classes = new HashMap<String, Class>();
	private HashMap<String, Function> functions = new HashMap<String, Function>();
	private HashMap<String, Module> modules = new HashMap<String, Module>();

	private boolean isSystem;
	private String name;
	private Module parent;

	public Module(org.python.pydev.parser.jython.ast.Module ast, String name,
			Module parent, boolean isSystem) {
		assert ast != null;

		this.ast = ast;
		this.name = name;
		this.parent = parent;
		this.isSystem = isSystem;
	}

	public void addClass(Class subclass) {
		classes.put(subclass.getName(), subclass);
	}

	public void addFunction(Function subfunction) {
		functions.put(subfunction.getName(), subfunction);
	}

	public void addModule(Module submodule) {
		modules.put(submodule.getName(), submodule);
	}

	public org.python.pydev.parser.jython.ast.Module getAst() {
		return ast;
	}

	public Cfg getCfg() {
		throw new Error("Not implemented yet");
	}

	public Map<String, Class> getClasses() {
		return Collections.unmodifiableMap(classes);
	}

	public String getFullName() {
		if (isTopLevel())
			return getName();
		else {
			String parentName = parent.getFullName();
			if (parentName.isEmpty())
				return getName();
			else
				return parentName + "." + getName();
		}
	}

	public Map<String, Function> getFunctions() {
		return Collections.unmodifiableMap(functions);
	}

	public Map<String, Module> getModules() {
		return Collections.unmodifiableMap(modules);
	}

	public String getName() {
		return name;
	}

	public Module getParent() {
		return parent;
	}

	public Namespace getParentScope() {
		return getParent();
	}

	public boolean isSystem() {
		return isSystem;
	}

	public boolean isTopLevel() {
		return getParent() == null;
	}

	public Module lookup(List<String> importNameTokens) {
		Queue<String> tokens = new LinkedList<String>(importNameTokens);

		Module scope = this;
		while (scope != null && !tokens.isEmpty()) {
			String token = tokens.remove();
			if (tokens.isEmpty())
				return scope.getModules().get(token);
			else
				scope = scope.getModules().get(token);
		}

		return scope;
	}

	@Override
	public String toString() {
		return "Module[" + getFullName() + "]";
	}
}
