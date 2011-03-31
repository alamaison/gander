package uk.ac.ic.doc.gander.hierarchy.build;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Builder {

	private static final Pattern MODULE_NAME_PATTERN = Pattern
			.compile("(.*)\\.py");
	private static final int MODULE_NAME_MATCHING_GROUP = 1;

	/**
	 * Convert file name into a Python module name.
	 * 
	 * @param module
	 *            Python module File.
	 * @return Module name if valid Python module filename. Null otherwise.
	 */
	protected static String moduleNameFromFile(File module) {
		if (!module.isFile())
			return null;
		Matcher m = MODULE_NAME_PATTERN.matcher(module.getName());
		if (m.matches())
			return m.group(MODULE_NAME_MATCHING_GROUP);
		return null;
	}
}