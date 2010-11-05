package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.FunctionDef;

public class Cfg {

	private FunctionDefScope scope;

	public Cfg(SimpleNode ast) throws Exception {
		FunctionDef func = (FunctionDef) ast;
		scope = new FunctionDefScope(func);
	}

	public BasicBlock getStart() {
		return scope.getStart();
	}

	public BasicBlock getEnd() {
		return scope.getEnd();
	}
}
