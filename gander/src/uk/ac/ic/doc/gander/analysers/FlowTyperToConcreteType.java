package uk.ac.ic.doc.gander.analysers;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.Feature;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.concretetype.ConcreteType;
import uk.ac.ic.doc.gander.concretetype.ConcreteTypeSystem;
import uk.ac.ic.doc.gander.concretetype.FiniteConcreteType;
import uk.ac.ic.doc.gander.concretetype.TopC;
import uk.ac.ic.doc.gander.flowinference.TypeEngine;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TObject;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.implementation.ClassImplementation;
import uk.ac.ic.doc.gander.implementation.Implementation;
import uk.ac.ic.doc.gander.implementation.InstanceImplementation;
import uk.ac.ic.doc.gander.model.ModelSite;

public final class FlowTyperToConcreteType implements ConcreteTypeSystem {

	private final TypeEngine cfaTyper;
	private final TypeResolver resolver;

	public FlowTyperToConcreteType(TypeEngine cfaTyper, TypeResolver resolver) {
		this.cfaTyper = cfaTyper;
		this.resolver = resolver;
	}

	@Override
	public ConcreteType typeOf(ModelSite<? extends exprType> expression,
			BasicBlock containingBlock) {

		Result<Type> flowType = cfaTyper.typeOf(expression);

		return flowType.transformResult(new Transformer<Type, ConcreteType>() {

			@Override
			public ConcreteType transformFiniteResult(Set<Type> result) {

				Set<Implementation> impls = new HashSet<Implementation>();

				for (Type value : result) {
					impls.add(abstractValueToImplementation(value));
				}

				return new FiniteConcreteType(impls);
			}

			private Implementation abstractValueToImplementation(Type value) {

				if (value instanceof TClass) {

					return new ClassImplementation(((TClass) value)
							.codeObject(), resolver);

				} else if (value instanceof TObject) {

					return new InstanceImplementation(((TObject) value)
							.classObject(), resolver);

				} else {
					return new Implementation() {

						@Override
						public boolean definesSupportFor(Feature feature) {

							return false;
						}
					};
				}
			}

			@Override
			public ConcreteType transformInfiniteResult() {

				return TopC.INSTANCE;
			}
		});
	}

}
