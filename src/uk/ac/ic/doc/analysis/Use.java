package uk.ac.ic.doc.analysis;

import org.python.pydev.parser.jython.ast.Name;

public class Use implements IDefUse {
	private Name name;

	Use(Name name) {
		this.name = name;
	}
	
	public Name getName() {
		return name;
	}

}
