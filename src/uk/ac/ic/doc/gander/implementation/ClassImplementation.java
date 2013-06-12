package uk.ac.ic.doc.gander.implementation;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.gander.Feature;
import uk.ac.ic.doc.gander.analysis.inheritance.CachingInheritanceTree;
import uk.ac.ic.doc.gander.analysis.inheritance.InheritedMethods;
import uk.ac.ic.doc.gander.ducktype.NamedMethodFeature;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;

public final class ClassImplementation implements Implementation {

	private final InheritedMethods inheritance;

	public ClassImplementation(ClassCO klass, TypeResolver resolver) {

		inheritance = new InheritedMethods(new CachingInheritanceTree(
				klass.oldStyleConflatedNamespace(), resolver));
	}

	@Override
	public boolean definesSupportFor(Feature feature) {

		// TODO: We only compare by name. Matching
		// parameter numbers etc
		// will require more complex logic.
		return features().contains(feature);
	}

	private Set<Feature> features() {
		Set<Feature> features = new HashSet<Feature>();

		for (String methodName : inheritance.methodsInTree()) {
			features.add(new NamedMethodFeature(methodName));
		}

		return features;
	}
}
