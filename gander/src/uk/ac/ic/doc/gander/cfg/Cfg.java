package uk.ac.ic.doc.gander.cfg;

import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.FunctionDef;

import uk.ac.ic.doc.gander.cfg.scope.FunctionDefScope;

public class Cfg {

	private FunctionDefScope scope;

	public Cfg(SimpleNode ast) throws Exception {
		scope = new FunctionDefScope((FunctionDef) ast);
		scope.process();
	}

	public BasicBlock getStart() {
		return scope.getStart();
	}

	public BasicBlock getEnd() {
		return scope.getEnd();
	}

	public BasicBlock getException() {
		return scope.getException();
	}

	public Set<BasicBlock> getBlocks() {
		return scope.getBlocks();
	}
}
