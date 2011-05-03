package uk.ac.ic.doc.gander.hierarchy;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HierarchyFactory {

	private static final String PYTHON_PATH_PROGRAM = "import sys\n"
			+ "for x in sys.path:\n    print x\n\n";

	public static Hierarchy createHierarchy(Iterable<File> topLevelPaths,
			Iterable<File> topLevelSystemPaths) throws InvalidElementException {
		return new Hierarchy(topLevelPaths, topLevelSystemPaths);
	}

	public static Hierarchy createHierarchy(Iterable<File> topLevelPaths)
			throws InvalidElementException {
		List<File> systemTopLevelPaths = new ArrayList<File>();
		for (String sysPath : HierarchyFactory.queryPythonPath("./pypy")) {
			systemTopLevelPaths.add(new File(sysPath));
		}
		for (String sysPath : HierarchyFactory.queryPythonPath("python")) {
			systemTopLevelPaths.add(new File(sysPath));
		}

		return createHierarchy(topLevelPaths, systemTopLevelPaths);
	}

	public static Hierarchy createHierarchy(File topLevel)
			throws InvalidElementException {
		return createHierarchy(directoryToList(topLevel));
	}

	private static Iterable<String> queryPythonPath(String pythonCommand) {
		try {
			String[] commands = { pythonCommand, "-c", PYTHON_PATH_PROGRAM };
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

	private static List<File> directoryToList(File directory) {
		List<File> directories = new ArrayList<File>();
		directories.add(directory);
		return directories;
	}
}
