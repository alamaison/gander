package uk.ac.ic.doc.gander.model;

import java.io.IOException;
import java.util.List;

import org.python.pydev.parser.jython.ParseException;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.model.build.TopLevelPackageLoader;

public class Model {

	protected Module topLevelPackage;
	protected Hierarchy hierarchy;

	public Model(Hierarchy hierarchy) throws ParseException,
			IOException {
		this.hierarchy = hierarchy;
		topLevelPackage = new TopLevelPackageLoader().getPackage();
	}

	public Module getTopLevel() {
		return topLevelPackage;
	}

	public Module lookup(String importName) {
		return lookup(DottedName.toImportTokens(importName));
	}
	
	public Module lookup(List<String> importNameTokens) {
		return getTopLevel().lookup(importNameTokens);
	}
}