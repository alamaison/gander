package uk.ac.ic.doc.gander.model;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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

	public Module lookupModule(List<String> importNameTokens) {
		Queue<String> tokens = new LinkedList<String>(importNameTokens);

		Package scope = getTopLevelPackage();
		while (scope != null && !tokens.isEmpty()) {
			String token = tokens.remove();
			if (tokens.isEmpty())
				return scope.getModules().get(token);
			else
				scope = scope.getPackages().get(token);
		}

		return null;
	}

	public Package lookupPackage(List<String> importNameTokens) {
		Queue<String> tokens = new LinkedList<String>(importNameTokens);

		Package scope = getTopLevelPackage();
		while (scope != null && !tokens.isEmpty()) {
			String token = tokens.remove();
			if (tokens.isEmpty())
				return scope.getPackages().get(token);
			else
				scope = scope.getPackages().get(token);
		}

		return null;
	}

	private static List<String> dottedNameToImportTokens(String importPath) {
		return Arrays.asList(importPath.split("\\."));
	}
}
