package uk.ac.ic.doc.analysis;

import org.python.pydev.parser.jython.ast.Name;

public class Def implements IDefUse {
	private Name name;

	Def(Name name) {
		this.name = name;
	}
	
	public Name getName() {
		return name;
	}
}
