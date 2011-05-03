package uk.ac.ic.doc.gander;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DottedName {

	public static List<String> toImportTokens(String importPath) {
		if ("".equals(importPath))
			return Collections.emptyList();
		return Arrays.asList(importPath.split("\\."));
	}

	public static String toDottedName(List<String> importTokens) {

		StringBuilder builder = new StringBuilder();
		Iterator<String> iter = importTokens.iterator();
		while (iter.hasNext()) {
			builder.append(iter.next());

			if (iter.hasNext())
				builder.append(".");
		}
		return builder.toString();
	}
}
