package uk.ac.ic.doc.gander.model;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import uk.ac.ic.doc.gander.model.build.PackageBuilder;

public class Model {

	private Package topLevelPackage;

	public Model(File topLevelDirectory) throws Exception {
		new PackageBuilder(topLevelDirectory, null, this).getPackage();
	}

	public Package getTopLevelPackage() {
		return topLevelPackage;
	}

	public void setTopLevelPackage(Package p) {
		topLevelPackage = p;
	}

	public Module lookupModule(String importName) {
		return lookupModule(dottedNameToImportTokens(importName));
	}

	public Package lookupPackage(String importName) {
		return lookupPackage(dottedNameToImportTokens(importName));
	}
	
	public Importable lookup(String importName) {
		List<String> tokens = dottedNameToImportTokens(importName);
		Importable imported = lookupPackage(tokens);
		if (imported == null)
			imported = lookupModule(tokens);
		return imported;
	}

	public Module lookupModule(List<String> importNameTokens) {
		return getTopLevelPackage().lookupModule(importNameTokens);	}

	public Package lookupPackage(List<String> importNameTokens) {
		return getTopLevelPackage().lookupPackage(importNameTokens);
	}

	private static List<String> dottedNameToImportTokens(String importPath) {
		if ("".equals(importPath))
			return Collections.emptyList();
		return Arrays.asList(importPath.split("\\."));
	}
}
