package uk.ac.ic.doc.gander;

import java.io.File;
import java.util.Arrays;

public abstract class MultiProjectRunner {

	public void run(String[] args) throws Exception {
		run(Arrays.asList(args));
	}

	public void run(Iterable<String> args) throws Exception {
		for (String arg : args) {
			analyseProject(new File(arg));
		}
	}

	protected abstract void analyseProject(File projectRoot) throws Exception;
}