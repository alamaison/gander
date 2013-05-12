package uk.ac.ic.doc.gander.ducktype;

import uk.ac.ic.doc.gander.interfacetype.Feature;

public final class NamedMethodFeature implements Feature {

	private final String name;

	NamedMethodFeature(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return "NamedMethodFeature [name=" + name + "]";
	}
	
}
