package uk.ac.ic.doc.gander.contraindication;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.Feature;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.concretetype.ConcreteType;
import uk.ac.ic.doc.gander.concretetype.ConcreteTypeSystem;
import uk.ac.ic.doc.gander.concretetype.FiniteConcreteType;
import uk.ac.ic.doc.gander.concretetype.TopC;
import uk.ac.ic.doc.gander.duckinference.LoadedTypeDefinitions;
import uk.ac.ic.doc.gander.ducktype.NamedMethodFeature;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.implementation.ClassImplementation;
import uk.ac.ic.doc.gander.implementation.Implementation;
import uk.ac.ic.doc.gander.implementation.InstanceImplementation;
import uk.ac.ic.doc.gander.interfacetype.InterfaceType;
import uk.ac.ic.doc.gander.interfacetype.InterfaceTypeSystem;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;

public final class Contraindication implements ConcreteTypeSystem {

	private final Model model;
	private final ConcreteTypeSystem concreteTypeSystem;
	private final InterfaceTypeSystem interfaceTypeSystem;
	private LoadedTypeDefinitions definitions = null;
	private TypeResolver resolver;

	public Contraindication(Model model, ConcreteTypeSystem concreteTypeSystem,
			InterfaceTypeSystem interfaceTypeSystem, TypeResolver resolver) {
		this.model = model;
		this.concreteTypeSystem = concreteTypeSystem;
		this.interfaceTypeSystem = interfaceTypeSystem;
		this.resolver = resolver;
	}

	public ConcreteType typeOf(ModelSite<? extends exprType> expression,
			BasicBlock containingBlock) {
		ConcreteType concreteType = concreteTypeSystem.typeOf(expression,
				containingBlock);
		InterfaceType interfaceType = interfaceTypeSystem.typeOf(expression,
				containingBlock);

		return filterConcreteType(concreteType, interfaceType);
	}

	private ConcreteType filterConcreteType(final ConcreteType concreteType,
			final InterfaceType interfaceType) {
		return concreteType
				.transformResult(new Transformer<Implementation, ConcreteType>() {

					@Override
					public ConcreteType transformFiniteResult(
							Set<Implementation> result) {

						return contraindicateType(interfaceType, result);
					}

					@Override
					public ConcreteType transformInfiniteResult() {

						return contraindicateTop(interfaceType);
					}
				});
	}

	private ConcreteType contraindicateTop(final InterfaceType interfaceType) {
		// Like DuckTyper
		// Does contraindication against Top

		Set<String> methods = new HashSet<String>();
		for (Feature feature : interfaceType) {
			if (feature instanceof NamedMethodFeature) {
				methods.add(((NamedMethodFeature) feature).name());
			}
		}

		if (methods.isEmpty()) {
			return TopC.INSTANCE;
		} else {
			return contraindicateType(interfaceType, loadedImplementations());
		}
	}

	private ConcreteType contraindicateType(final InterfaceType interfaceType,
			Set<Implementation> result) {

		Set<Implementation> compatibleImplementations = new HashSet<Implementation>();

		for (Implementation implementation : result) {
			if (compatible(implementation, interfaceType)) {
				compatibleImplementations.add(implementation);
			}
		}

		return new FiniteConcreteType(compatibleImplementations);
	}

	private boolean compatible(Implementation implementation,
			InterfaceType interfaceType) {

		// An implementation is compatible with an interface if it supports all
		// the features in the interface
		for (Feature feature : interfaceType) {
			if (!implementation.definesSupportFor(feature)) {
				return false;
			}
		}

		return true;
	}

	private Set<Implementation> loadedImplementations() {

		Set<Implementation> topImplementations = new HashSet<Implementation>();

		for (ClassCO klass : definitions().getDefinitions()) {

			/*
			 * TODO: Both classes and class instances use the class as the
			 * implementation and both allow the method to be called with the
			 * same syntax. The difference is that calling it via the class
			 * means passing self in as an explicit extra parameter. At the
			 * moment we don't look at parameters when matching named method
			 * features in either case, so we can't exlude one from Top but not
			 * the other.
			 */
			topImplementations.add(new InstanceImplementation(klass, resolver));
			topImplementations.add(new ClassImplementation(klass, resolver));
		}

		return topImplementations;
	}

	private LoadedTypeDefinitions definitions() {
		if (definitions == null)
			definitions = new LoadedTypeDefinitions(model);

		return definitions;
	}
}
