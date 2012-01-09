package uk.ac.ic.doc.gander.importing;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

import uk.ac.ic.doc.gander.DottedName;

/**
 * Model of the dotted names used in Python import statements.
 * 
 * Both the standard and from-style imports use dotted names that, unlike dotted
 * names elsewhere in Python, don't necessarily represent attribute access on an
 * object. However, it can often seem as though it does making it extremely
 * confusing.
 */
public final class ImportPath extends AbstractList<String> {

	private final List<String> path;

	public static ImportPath fromDottedName(String dottedName) {
		if (dottedName == null)
			throw new NullPointerException("Path not optional");
		if (dottedName.isEmpty())
			throw new IllegalArgumentException("Empty path is not valid");

		return fromTokens(DottedName.toImportTokens(dottedName));
	}

	public static ImportPath fromTokens(List<String> tokens) {
		if (tokens == null)
			throw new NullPointerException("Path not optional");
		if (tokens.isEmpty())
			throw new IllegalArgumentException("Empty path is not valid");

		return new ImportPath(tokens);
	}

	public String dottedName() {
		return DottedName.toDottedName(path);
	}

	private ImportPath(List<String> path) {
		this.path = Collections.unmodifiableList(path);
	}

	@Override
	public String get(int index) {
		return path.get(index);
	}

	@Override
	public int size() {
		return path.size();
	}

	@Override
	public String toString() {
		return "ImportPath [path=" + path + "]";
	}

}
