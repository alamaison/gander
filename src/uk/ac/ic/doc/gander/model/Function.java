package uk.ac.ic.doc.gander.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.NameTok;

import uk.ac.ic.doc.gander.cfg.model.Cfg;
import uk.ac.ic.doc.gander.model.build.BuildableScope;

public class Function implements BuildableScope {

	private FunctionDef function;
	private Scope parent;
	private Map<String, Function> functions = new HashMap<String, Function>();
	private Map<String, Class> classes = new HashMap<String, Class>();

	private Cfg graph = null;

	public Function(FunctionDef function, Scope parent) {
		this.function = function;
		this.parent = parent;
	}

	public String getName() {
		return ((NameTok) (function.name)).id;
	}

	public String getFullName() {
		return parent.getFullName() + "." + getName();
	}

	public Cfg getCfg() throws Exception {
		if (graph == null)
			graph = new Cfg(function);
		return graph;
	}

	public FunctionDef getFunctionDef() {
		return function;
	}

	public Scope getParentScope() {
		return parent;
	}

	public Map<String, Package> getPackages() {
		return Collections.emptyMap();
	}

	public Map<String, Module> getModules() {
		return Collections.emptyMap();
	}

	public Map<String, Class> getClasses() {
		return Collections.unmodifiableMap(classes);
	}

	public Map<String, Function> getFunctions() {
		return Collections.unmodifiableMap(functions);
	}

	public void addPackage(Package pkg) {
		throw new Error("A function cannot contain a package");
	}

	public void addModule(Module module) {
		throw new Error("A function cannot contain a package");
	}

	public void addClass(Class klass) {
		classes.put(klass.getName(), klass);
	}

	public void addFunction(Function function) {
		functions.put(function.getName(), function);
	}
}
