package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.NameTok;

public class Method implements IModelElement {

	private FunctionDef method;

	public Method(FunctionDef method) {
		this.method = method;
	}

	public String getName() {
		return ((NameTok) (method.name)).id;
	}

	public Cfg getCfg() throws Exception {
		return new Cfg(method);
	}

}
