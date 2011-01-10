package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.ast.FunctionDef;

public class Method extends Function {

	protected Method(FunctionDef method, IModelElement parent) {
		super(method, parent);
	}

}
