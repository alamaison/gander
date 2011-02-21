package uk.ac.ic.doc.gander.model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.python.pydev.parser.jython.ParseException;
import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.stmtType;

import uk.ac.ic.doc.gander.model.build.ModuleParser;


public class Module implements Scope {

	private String name;
	private org.python.pydev.parser.jython.ast.Module module;
	private Package parent;


	protected Module(File module, Package parent) throws IOException,
			ParseException, InvalidElementException {
		ModuleParser parser = new ModuleParser(module);
		this.module = parser.getAst();
		this.name = parser.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ic.doc.cfg.model.IModelElement#getName()
	 */
	public String getName() {
		return name;
	}

	public String getFullName() {
		return parent.getFullName() + "." + getName();
	}

	public SimpleNode getAst() {
		return module;
	}
	
	public Package getPackage() {
		return parent;
	}

	public Scope getParentScope() {
		return getPackage();
	}

	public Map<String, Class> getClasses() {

		Map<String, Class> classes = new HashMap<String, Class>();
		for (stmtType stmt : module.body) {
			if (stmt instanceof ClassDef) {
				Class cls = new Class((ClassDef) stmt, this);
				classes.put(cls.getName(), cls);
			}
		}

		return classes;
	}

	public Map<String, Function> getFunctions() {

		Map<String, Function> functions = new HashMap<String, Function>();
		for (stmtType stmt : module.body) {
			if (stmt instanceof FunctionDef) {
				Function function = new Function((FunctionDef) stmt, this);
				functions.put(function.getName(), function);
			}
		}

		return functions;
	}

	public Scope lookup(String token) {
		// FIXME: This order is arbitrary. Really we should record which
		// function/class definition came last and only use that one
		Scope subItem = getClasses().get(token);
		if (subItem == null)
			subItem = getFunctions().get(token);

		return subItem;
	}
}
