package uk.ac.ic.doc.cfg.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.python.pydev.parser.IGrammar;
import org.python.pydev.parser.grammar26.PythonGrammar26;
import org.python.pydev.parser.jython.CharStream;
import org.python.pydev.parser.jython.FastCharStream;
import org.python.pydev.parser.jython.ParseException;
import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.stmtType;

public class Module implements IModelElement {

	private String name;
	private org.python.pydev.parser.jython.ast.Module module;
	private IModelElement parent;

	private static final Pattern MODULE_NAME_PATTERN = Pattern
			.compile("(.*)\\.py");
	private static final int MODULE_NAME_MATCHING_GROUP = 1;

	protected Module(File module, IModelElement parent) throws IOException,
			ParseException, InvalidElementException {
		this.name = moduleNameFromFile(module);
		if (this.name == null)
			throw new InvalidElementException("Not a module", module);
		this.parent = parent;

		if (module.length() > 0) {
			CharStream stream = new FastCharStream(fileToString(module)
					.toCharArray());
			IGrammar grammar = new PythonGrammar26(stream);
			this.module = (org.python.pydev.parser.jython.ast.Module) grammar
					.file_input();
		} else {
			this.module = new org.python.pydev.parser.jython.ast.Module(null);
			this.module.body = new stmtType[] {};
		}
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

	/**
	 * Read file contents into a String.
	 */
	private static String fileToString(File file) throws IOException {
		byte[] buffer = new byte[(int) file.length()];
		BufferedInputStream f = null;
		try {
			f = new BufferedInputStream(new FileInputStream(file));
			f.read(buffer);
		} finally {
			if (f != null)
				try {
					f.close();
				} catch (IOException ignored) {
				}
		}
		return new String(buffer);
	}

	/**
	 * Convert file name into a Python module name.
	 * 
	 * @param module
	 *            Python module File.
	 * @return Module name if valid Python module filename. Null otherwise.
	 */
	protected static String moduleNameFromFile(File module) {
		if (!module.isFile())
			return null;
		Matcher m = MODULE_NAME_PATTERN.matcher(module.getName());
		if (m.matches())
			return m.group(MODULE_NAME_MATCHING_GROUP);
		return null;
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
}
