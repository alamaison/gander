package uk.ac.ic.doc.gander.cfg;

import org.python.pydev.parser.IGrammar;
import org.python.pydev.parser.grammar26.PythonGrammar26;
import org.python.pydev.parser.jython.CharStream;
import org.python.pydev.parser.jython.ParseException;
import org.python.pydev.parser.jython.SimpleNode;

class Parser {

	static SimpleNode parse(CharStream stream) throws ParseException {
		IGrammar grammar = new PythonGrammar26(stream);
		return grammar.file_input();
	}

}
