package uk.ac.ic.doc.gander.model;

import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.NameTok;

import uk.ac.ic.doc.gander.cfg.model.Cfg;

public class Function implements Scope {

	private FunctionDef function;
	private Scope parent;
	
	private Cfg graph = null;

	protected Function(FunctionDef function, Scope parent) {
		this.function = function;
		this.parent = parent;
	}

	public String getName() {
		return ((NameTok) (function.name)).id;
	}
	
	public String getFullName() {
		return parent.getFullName() + "." + getName();
	}

	public Cfg getCfg() throws Exception {
		if (graph == null)
			graph = new Cfg(function);
		return graph;
	}
	
	public FunctionDef getFunctionDef() {
		return function;
	}
	
	public Scope getParentScope() {
		return parent;
	}

	public Scope lookup(String token) {
		return null;
	}

}
