package uk.ac.ic.doc.gander.hierarchy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uk.ac.ic.doc.gander.hierarchy.build.PackageBuilder;

public class Hierarchy {

	private Package topLevel;

	public Hierarchy(File topLevelDirectory) throws InvalidElementException {
		this(directoryToList(topLevelDirectory));
	}

	public Hierarchy(Iterable<File> topLevelDirectories)
			throws InvalidElementException {
		PackageBuilder builder = new PackageBuilder(topLevelDirectories);
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

	private static List<File> directoryToList(File directory) {
		List<File> directories = new ArrayList<File>();
		directories.add(directory);
		return directories;
	}
}
