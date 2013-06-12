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
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyClass;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyInstance;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
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

		Result<PyObject> flowType = cfaTyper.typeOf(expression);

		return flowType.transformResult(new Transformer<PyObject, ConcreteType>() {

			@Override
			public ConcreteType transformFiniteResult(Set<PyObject> result) {

				Set<Implementation> impls = new HashSet<Implementation>();

				for (PyObject value : result) {
					impls.add(abstractValueToImplementation(value));
				}

				return new FiniteConcreteType(impls);
			}

			private Implementation abstractValueToImplementation(PyObject value) {

				if (value instanceof PyClass) {

					return new ClassImplementation(((PyClass) value)
							.codeObject(), resolver);

				} else if (value instanceof PyInstance) {

					return new InstanceImplementation(((PyInstance) value)
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
