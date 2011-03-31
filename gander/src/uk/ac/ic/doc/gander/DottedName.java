package uk.ac.ic.doc.gander;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DottedName {

	public static List<String> toImportTokens(String importPath) {
		if ("".equals(importPath))
			return Collections.emptyList();
		return Arrays.asList(importPath.split("\\."));
	}
}
