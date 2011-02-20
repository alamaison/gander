package uk.ac.ic.doc.gander.cfg.model;

import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.NameTok;

import uk.ac.ic.doc.gander.cfg.model.Cfg;

public class Function implements IModelElement {

	private FunctionDef function;
	private IModelElement parent;

	protected Function(FunctionDef function, IModelElement parent) {
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
		return new Cfg(function);
	}
	
	public FunctionDef getFunctionDef() {
		return function;
	}
	
	public IModelElement getParent() {
		return parent;
	}

}
