package uk.ac.ic.doc.gander.model.build;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.python.pydev.parser.IGrammar;
import org.python.pydev.parser.grammar26.PythonGrammar26;
import org.python.pydev.parser.jython.CharStream;
import org.python.pydev.parser.jython.FastCharStream;
import org.python.pydev.parser.jython.ParseException;
import org.python.pydev.parser.jython.TokenMgrError;
import org.python.pydev.parser.jython.ast.Module;
import org.python.pydev.parser.jython.ast.stmtType;

public class ModuleParser {

	private Module module;

	public ModuleParser(File module) throws ParseException, IOException {

		try {
			if (module.length() > 0) {
				CharStream stream = new FastCharStream(fileToString(module)
						.toCharArray());
				IGrammar grammar = new PythonGrammar26(stream);
				this.module = (org.python.pydev.parser.jython.ast.Module) grammar
						.file_input();
			} else {
				this.module = new org.python.pydev.parser.jython.ast.Module(
						null);
				this.module.body = new stmtType[] {};
			}
		} catch (TokenMgrError e) {
			System.err.println("PARSING FAILED: " + module);
			throw new ParseException(e.toString(), e.errorLine, e.errorColumn);
		} catch (Error e) {
			System.err.println("PARSING FAILED: " + module);
			throw e;
		} catch (ParseException e) {
			System.err.println("PARSING FAILED: " + module);
			throw e;
		}
	}

	public Module getAst() {
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
}
