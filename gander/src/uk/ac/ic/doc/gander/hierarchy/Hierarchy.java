package uk.ac.ic.doc.gander.hierarchy;

import java.io.File;
import java.util.List;

import uk.ac.ic.doc.gander.hierarchy.build.PackageBuilder;

public class Hierarchy {

	private Package topLevel;

	Hierarchy(Iterable<File> topLevelDirectories,
			Iterable<File> topLevelSystemDirectories)
			throws InvalidElementException {
		PackageBuilder builder = new PackageBuilder(topLevelDirectories,
				topLevelSystemDirectories);
		this.topLevel = builder.getPackage();
	}

	public Package getTopLevelPackage() {
		return topLevel;
	}

	public Module findModule(String fullyQualifiedName) {
		return getTopLevelPackage().findModule(fullyQualifiedName);
	}

	public Package findPackage(String fullyQualifiedName) {
		return getTopLevelPackage().findPackage(fullyQualifiedName);
	}

	public Module findModule(List<String> fullyQualifiedName) {
		return getTopLevelPackage().findModule(fullyQualifiedName);
	}

	public Package findPackage(List<String> fullyQualifiedName) {
		return getTopLevelPackage().findPackage(fullyQualifiedName);
	}
}
