package uk.ac.ic.doc.gander;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.InvalidElementException;

public class TestHierarchyBuilder {

	public static final String PYTHON_PATH_PROGRAM = "import sys\n"
			+ "for x in sys.path:\n    print x\n\n";

	public static Hierarchy createHierarchy(File topLevel)
			throws URISyntaxException, InvalidElementException {
		List<File> paths = new ArrayList<File>();
		for (String sysPath : TestHierarchyBuilder.queryPythonPath()) {
			paths.add(new File(sysPath));
		}
		paths.add(topLevel);

		return new Hierarchy(paths);
	}

	public static Hierarchy createHierarchyNoLibrary(String caseName,
			File topLevel) throws URISyntaxException, InvalidElementException {
		List<File> paths = new ArrayList<File>();
		paths.add(topLevel);

		return new Hierarchy(paths);
	}

	private static Iterable<String> queryPythonPath() {
		try {
			String[] commands = { "python", "-c", PYTHON_PATH_PROGRAM };
			Process python = Runtime.getRuntime().exec(commands);
			InputStream output = python.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					output));

			List<String> path = new ArrayList<String>();

			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				path.add(line);
			}
			return path;
		} catch (IOException e) {
			// If we fail because, for instance Python doesn't exist on the
			// system use empty Python path.
			return Collections.emptyList();
		}
	}
}
