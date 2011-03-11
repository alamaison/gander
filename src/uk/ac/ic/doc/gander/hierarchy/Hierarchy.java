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

	public Module lookupModule(String fullyQualifiedName) {
		return getTopLevelPackage().lookupModule(fullyQualifiedName);
	}

	public Package lookupPackage(String fullyQualifiedName) {
		return getTopLevelPackage().lookupPackage(fullyQualifiedName);
	}

	public Module lookupModule(List<String> fullyQualifiedName) {
		return getTopLevelPackage().lookupModule(fullyQualifiedName);
	}

	public Package lookupPackage(List<String> fullyQualifiedName) {
		return getTopLevelPackage().lookupPackage(fullyQualifiedName);
	}

	private static List<File> directoryToList(File directory) {
		List<File> directories = new ArrayList<File>();
		directories.add(directory);
		return directories;
	}
}
