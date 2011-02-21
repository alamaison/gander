package uk.ac.ic.doc.gander.model;

import org.python.pydev.parser.jython.ast.FunctionDef;

public class Method extends Function {

	protected Method(FunctionDef method, Scope parent) {
		super(method, parent);
	}

}
