package uk.ac.ic.doc.gander.importing;

import java.util.AbstractList;
import java.util.ArrayList;
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

		return fromTokens(DottedName.toImportTokens(dottedName));
	}

	public static ImportPath fromTokens(List<String> tokens) {
		if (tokens == null)
			throw new NullPointerException("Path not optional");

		return new ImportPath(tokens);
	}

	public static final ImportPath EMPTY_PATH = fromTokens(Collections
			.<String> emptyList());

	public ImportPath append(String itemName) {
		List<String> newTokens = new ArrayList<String>(this);
		newTokens.add(itemName);
		return fromTokens(newTokens);
	}

	public ImportPath subPath(int fromIndex, int toIndex) {
		List<String> subList = subList(fromIndex, toIndex);
		return fromTokens(subList);
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
		return dottedName();
	}

}
