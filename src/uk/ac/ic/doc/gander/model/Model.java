package uk.ac.ic.doc.gander.model;

import java.io.File;
import java.io.IOException;

import org.python.pydev.parser.jython.ParseException;


public class Model {

	private Package topLevelPackage;

	public Model(File topLevelDirectory) throws IOException, ParseException,
			InvalidElementException {
		topLevelPackage = new Package(topLevelDirectory);
	}

	public Package getTopLevelPackage() {
		return topLevelPackage;
	}

}
