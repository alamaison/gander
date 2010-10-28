package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.NameTok;

public class Function implements IModelElement {

	private FunctionDef function;

	public Function(FunctionDef function) {
		this.function = function;
	}

	public String getName() {
		return ((NameTok) (function.name)).id;
	}

}
