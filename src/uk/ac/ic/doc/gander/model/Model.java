package uk.ac.ic.doc.gander.model;

import java.io.File;

import uk.ac.ic.doc.gander.model.build.PackageBuilder;

public class Model {

	private Package topLevelPackage;

	public Model(File topLevelDirectory) throws Exception {
		topLevelPackage = new PackageBuilder(topLevelDirectory, null)
				.getPackage();
	}

	public Package getTopLevelPackage() {
		return topLevelPackage;
	}

}
