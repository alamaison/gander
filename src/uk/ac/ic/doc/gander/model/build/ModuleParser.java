package uk.ac.ic.doc.gander.model.build;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.python.pydev.parser.IGrammar;
import org.python.pydev.parser.grammar26.PythonGrammar26;
import org.python.pydev.parser.jython.CharStream;
import org.python.pydev.parser.jython.FastCharStream;
import org.python.pydev.parser.jython.ParseException;
import org.python.pydev.parser.jython.ast.Module;
import org.python.pydev.parser.jython.ast.stmtType;

import uk.ac.ic.doc.gander.model.InvalidElementException;

public class ModuleParser {

	private static final Pattern MODULE_NAME_PATTERN = Pattern
			.compile("(.*)\\.py");
	private static final int MODULE_NAME_MATCHING_GROUP = 1;

	private String name;
	private Module module;

	public ModuleParser(File module) throws IOException, ParseException,
			InvalidElementException {
		this.name = moduleNameFromFile(module);
		if (this.name == null)
			throw new InvalidElementException("Not a module", module);

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

	public Module getAst() {
		return module;
	}

	public String getName() {
		return name;
	}

	/**
	 * Convert file name into a Python module name.
	 * 
	 * @param module
	 *            Python module File.
	 * @return Module name if valid Python module filename. Null otherwise.
	 */
	public static String moduleNameFromFile(File module) {
		if (!module.isFile())
			return null;
		Matcher m = MODULE_NAME_PATTERN.matcher(module.getName());
		if (m.matches())
			return m.group(MODULE_NAME_MATCHING_GROUP);
		return null;
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
}
