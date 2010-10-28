package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.NameTok;

public class Class implements IModelElement {

	private ClassDef cls;

	public Class(ClassDef cls) {
		this.cls = cls;
	}

	public String getName() {
		return ((NameTok)(cls.name)).id;
	}

}
