package uk.ac.ic.doc.gander.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.NameTok;

import uk.ac.ic.doc.gander.model.build.BuildableScope;

public class Class implements BuildableScope {

	private Map<String, Function> methods = new HashMap<String, Function>();
	private Map<String, Class> classes = new HashMap<String, Class>();

	private ClassDef cls;
	private Scope parent;

	public Class(ClassDef cls, Scope parent) {
		this.cls = cls;
		this.parent = parent;
	}

	public String getName() {
		return ((NameTok) (cls.name)).id;
	}

	public String getFullName() {
		return parent.getFullName() + "." + getName();
	}

	public ClassDef getClassDef() {
		return cls;
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
		return Collections.unmodifiableMap(methods);
	}

	public void addPackage(Package pkg) {
		throw new Error("A class cannot contain a package");
	}

	public void addModule(Module module) {
		throw new Error("A class cannot contain a package");
	}

	public void addClass(Class klass) {
		classes.put(klass.getName(), klass);
	}

	public void addFunction(Function function) {
		methods.put(function.getName(), function);
	}
}
