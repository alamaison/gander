package uk.ac.ic.doc.gander.ducktype;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uk.ac.ic.doc.gander.interfacetype.Feature;
import uk.ac.ic.doc.gander.interfacetype.InterfaceType;

/**
 * Interface type inferred from feature-usage in Python.
 */
public final class DuckType extends AbstractSet<Feature> implements
		InterfaceType {

	private final Set<Feature> features;

	@Override
	public Iterator<Feature> iterator() {
		return features.iterator();
	}

	@Override
	public int size() {
		return features.size();
	}

	DuckType(Collection<? extends Feature> methods) {
		this.features = Collections.unmodifiableSet(new HashSet<Feature>(
				methods));
	}

}
