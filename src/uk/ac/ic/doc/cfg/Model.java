package uk.ac.ic.doc.cfg;

import java.io.File;
import java.io.IOException;

import org.python.pydev.parser.jython.ParseException;

import uk.ac.ic.doc.cfg.model.InvalidElementException;
import uk.ac.ic.doc.cfg.model.Package;

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
