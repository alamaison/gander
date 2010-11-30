package uk.ac.ic.doc.cfg.model;

import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.FunctionDef;

import uk.ac.ic.doc.cfg.model.scope.FunctionDefScope;

public class Cfg {

	private FunctionDefScope scope;

	public Cfg(SimpleNode ast) throws Exception {
		FunctionDef func = (FunctionDef) ast;
		scope = new FunctionDefScope(func);
		scope.process();
	}

	public BasicBlock getStart() {
		return scope.getStart();
	}

	public BasicBlock getEnd() {
		return scope.getEnd();
	}

	public Set<BasicBlock> getBlocks() {
		return scope.getBlocks();
	}
}
