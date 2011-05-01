package uk.ac.ic.doc.gander.model;

import java.io.IOException;
import java.util.List;

import org.python.pydev.parser.jython.ParseException;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.model.build.TopLevelPackageLoader;

public class Model {

	protected Package topLevelPackage;
	protected Hierarchy hierarchy;

	public Model(Hierarchy hierarchy) throws ParseException,
			IOException {
		this.hierarchy = hierarchy;
		topLevelPackage = new TopLevelPackageLoader().getPackage();
	}

	public Package getTopLevelPackage() {
		return topLevelPackage;
	}

	public Module lookupModule(String importName) {
		return lookupModule(DottedName.toImportTokens(importName));
	}

	public Package lookupPackage(String importName) {
		return lookupPackage(DottedName.toImportTokens(importName));
	}

	public Module lookupModule(List<String> importNameTokens) {
		return getTopLevelPackage().lookupModule(importNameTokens);
	}

	public Package lookupPackage(List<String> importNameTokens) {
		return getTopLevelPackage().lookupPackage(importNameTokens);
	}

	public Loadable lookup(String importName) {
		List<String> tokens = DottedName.toImportTokens(importName);
		Loadable imported = lookupPackage(tokens);
		if (imported == null)
			imported = lookupModule(tokens);
		return imported;
	}
}