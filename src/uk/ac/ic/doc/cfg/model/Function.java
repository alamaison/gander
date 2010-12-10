package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.NameTok;

import uk.ac.ic.doc.cfg.model.Cfg;

public class Function implements IModelElement {

	private FunctionDef function;
	private IModelElement parent;

	public Function(FunctionDef function, IModelElement parent) {
		this.function = function;
		this.parent = parent;
	}

	public String getName() {
		return parent.getName() + "." + ((NameTok) (function.name)).id;
	}

	public Cfg getCfg() throws Exception {
		return new Cfg(function);
	}

}
