package uk.ac.ic.doc.gander.flowinference.types;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.Namespace;
import uk.ac.ic.doc.gander.flowinference.TopI;
import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.argument.CallsiteArgument;
import uk.ac.ic.doc.gander.flowinference.argument.SelfCallsiteArgument;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopFp;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopP;
import uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow.ReceivingParameterPositioner;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Processor;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.typegoals.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.typegoals.NamespaceNameTypeGoal;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.parameters.FormalParameter;

public class TClass implements TCodeObject, TCallable {

	private final class ReceivingParameterFinder implements
			Transformer<Type, Result<ArgumentDestination>> {

		private final Argument argument;
		private final SubgoalManager goalManager;

		ReceivingParameterFinder(Argument argument, SubgoalManager goalManager) {
			this.argument = argument;
			this.goalManager = goalManager;
		}

		@Override
		public Result<ArgumentDestination> transformFiniteResult(
				Set<Type> initImplementations) {

			RedundancyEliminator<ArgumentDestination> destinations = new RedundancyEliminator<ArgumentDestination>();

			for (Type initType : initImplementations) {

				if (initType instanceof TCallable) {

					destinations.add(((TCallable) initType)
							.destinationsReceivingArgument(argument,
									goalManager));

				} else {

					// TODO: init might be a callable object

					System.err.println("UNTYPABLE: __init__ "
							+ "implementation appears not to be callable: "
							+ initType);
				}

				if (destinations.isFinished()) {
					break;
				}
			}

			return destinations.result();
		}

		@Override
		public Result<ArgumentDestination> transformInfiniteResult() {
			return TopP.INSTANCE;
		}

	}

	private final ClassCO classObject;

	public TClass(ClassCO classInstance) {
		if (classInstance == null) {
			throw new NullPointerException("Code object required");
		}

		this.classObject = classInstance;
	}

	@Override
	public ClassCO codeObject() {
		return classObject;
	}

	@Deprecated
	public TClass(Class classInstance) {
		this(classInstance.codeObject());
	}

	@Deprecated
	public Class getClassInstance() {
		return classObject.oldStyleConflatedNamespace();
	}

	@Override
	public String getName() {
		return getClassInstance().getFullName();
	}

	@Override
	public Result<Type> returnType(SubgoalManager goalManager) {
		/*
		 * Calling a class is a constructor call. Constructors are special
		 * functions so we can infer the return type immediately. It is an
		 * instance of the class being called.
		 */
		return new FiniteResult<Type>(Collections.singleton(new TObject(
				classObject)));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Members on a class are returned directly from the class namespace.
	 */
	@Override
	public Result<Type> memberType(String memberName, SubgoalManager goalManager) {

		NamespaceName member = new NamespaceName(memberName,
				classObject.fullyQualifiedNamespace());
		return goalManager.registerSubgoal(new NamespaceNameTypeGoal(member));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Namespace> memberReadableNamespaces() {
		return Collections.<Namespace> singleton(classObject
				.fullyQualifiedNamespace());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Namespace memberWriteableNamespace() {
		return classObject.fullyQualifiedNamespace();
	}

	@Override
	public Result<Type> typeOfArgumentPassedToParameter(
			FormalParameter parameter, ModelSite<Call> callSite,
			SubgoalManager goalManager) {

		Result<Type> unboundInitMethodTypes = initMethodTypes(goalManager);

		return unboundInitMethodTypes
				.transformResult(new InitMethodParameterTyper(parameter,
						callSite, goalManager));
	}

	private class InitMethodParameterTyper implements
			Transformer<Type, Result<Type>> {

		private final FormalParameter parameter;
		private final ModelSite<Call> callSite;
		private final SubgoalManager goalManager;

		InitMethodParameterTyper(FormalParameter parameter,
				ModelSite<Call> callSite, SubgoalManager goalManager) {
			this.parameter = parameter;
			this.callSite = callSite;
			this.goalManager = goalManager;
		}

		@Override
		public Result<Type> transformFiniteResult(
				Set<Type> unboundInitMethodTypes) {

			RedundancyEliminator<Type> parameterType = new RedundancyEliminator<Type>();

			for (Type initType : unboundInitMethodTypes) {

				if (initType instanceof TCallable) {

					/*
					 * The unbound init method has no idea what type it's self
					 * argument might have. It knows nothing about self. For
					 * that we need to bind it.
					 */

					TBoundMethod boundInitMethod = new TBoundMethod(
							(TCallable) initType, new TObject(classObject));

					parameterType.add(boundInitMethod
							.typeOfArgumentPassedToParameter(parameter,
									callSite, goalManager));
				} else {
					// XXX: init might not be a function?!
					parameterType.add(TopT.INSTANCE);
				}

				if (parameterType.isFinished()) {
					break;
				}
			}

			return parameterType.result();
		}

		@Override
		public Result<Type> transformInfiniteResult() {
			/*
			 * Can't work out what __init__ implementations could be called so
			 * we certainly can't work out the type of the self parameter
			 */
			return TopT.INSTANCE;
		}
	};

	/**
	 * {@inheritDoc}
	 * 
	 * Ordinal arguments passed to a call to constructor are passed to the
	 * parameter of the receiver that is one further along the parameter list
	 * than the ordinal.
	 */
	@Override
	public Result<ArgumentDestination> destinationsReceivingArgument(
			CallsiteArgument argument, SubgoalManager goalManager) {
		if (argument == null) {
			throw new NullPointerException("Argument is not optional");
		}
		if (goalManager == null) {
			throw new NullPointerException(
					"Goal manager required to resolve constructors");
		}

		Argument actualArgument = argument
				.mapToActualArgument(new MethodStylePassingStrategy());

		return destinationsReceivingArgument(actualArgument, goalManager);
	}

	@Override
	public Result<ArgumentDestination> destinationsReceivingArgument(
			Argument argument, SubgoalManager goalManager) {
		if (argument == null) {
			throw new NullPointerException("Argument is not optional");
		}
		if (goalManager == null) {
			throw new NullPointerException(
					"Goal manager required to resolve constructors");
		}

		Result<Type> initMethodTypes = initMethodTypes(goalManager);

		return initMethodTypes.transformResult(new ReceivingParameterFinder(
				argument, goalManager));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Even if a class object is retrieved as an attribute of another object and
	 * called, that other object doesn't flow anywhere. In other words, a class
	 * object has no self parameter. The class object's constructor might but
	 * that is not the same thing; this method is about flowing the object on
	 * the LHS of the attribute.
	 */
	@Deprecated
	@Override
	public FormalParameter selfParameter() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The code object answering a call made on a class object may be any of the
	 * code objects assigned to the {@code __init__} member of the code object's
	 * namespace.
	 */
	@Override
	public Result<InvokableCodeObject> codeObjectsInvokedByCall(
			SubgoalManager goalManager) {
		if (goalManager == null) {
			throw new NullPointerException("Goal manager required");
		}

		Result<Type> initObjects = initMethodTypes(goalManager);

		return initObjects
				.transformResult(new InitCodeObjectFinder(goalManager));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * We model this for results of constructor calls by flowing the value to
	 * the positions of the {@code self} parameter in each method.
	 * 
	 * XXX: This is a bit of a hack. Really, the result should only be flowed at
	 * the method call site except for {@code __init__}.
	 * 
	 * FIXME: Doesn't work if the method was added by assignment rather than
	 * declaration.
	 */
	@Override
	public Result<FlowPosition> flowPositionsCausedByCalling(
			SubgoalManager goalManager) {

		RedundancyEliminator<FlowPosition> positions = new RedundancyEliminator<FlowPosition>();

		/*
		 * The value only flows to the most specific overload of each method so
		 * we keep track of which methods we've seen already so we can ignore
		 * them as we walk further up the inheritance tree.
		 * 
		 * XXX: I don't know if we walk the tree in the correct order.
		 */
		Set<String> doneMethods = new HashSet<String>();

		flowToMethodsOfClass(this, doneMethods, new HashSet<TClass>(),
				goalManager, positions);

		return positions.result();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * When a class object is called as an attribute of another object, nothing
	 * special happens. That object doesn't flow anywhere.
	 */
	@Override
	public Result<FlowPosition> flowPositionsOfHiddenSelfArgument(
			SubgoalManager goalManager) {

		return FiniteResult.bottom();
	}

	private void flowToMethodsOfClass(TClass klass, Set<String> doneMethods,
			Set<TClass> doneClasses, SubgoalManager goalManager,
			RedundancyEliminator<FlowPosition> positions) {

		/*
		 * This prevents a stack overflow in the case that the class appears to
		 * inherit from itself or a grandparent.
		 */
		doneClasses.add(klass);

		Collection<Function> methods = klass.codeObject()
				.oldStyleConflatedNamespace().getFunctions().values();

		for (Function method : methods) {

			if (positions.isFinished())
				break;

			TBoundMethod boundMethod = new TBoundMethod(new TFunction(
					method.codeObject()), new TObject(classObject));

			Result<ArgumentDestination> selfDestinations = boundMethod
					.destinationsReceivingArgument(new SelfCallsiteArgument(),
							goalManager);

			positions.add(selfDestinations
					.transformResult(new ReceivingParameterPositioner()));
		}

		for (exprType base : klass.codeObject().ast().bases) {

			flowToMethodsOfSuperClass(new ModelSite<exprType>(base, klass
					.codeObject().parent()), doneMethods, doneClasses,
					positions, goalManager);

			if (positions.isFinished())
				break;
		}

	}

	private void flowToMethodsOfSuperClass(
			final ModelSite<exprType> superclass,
			final Set<String> doneMethods, final Set<TClass> doneClasses,
			final RedundancyEliminator<FlowPosition> positions,
			final SubgoalManager goalManager) {

		Result<Type> superclassTypes = goalManager
				.registerSubgoal(new ExpressionTypeGoal(superclass));

		superclassTypes.actOnResult(new Processor<Type>() {

			@Override
			public void processInfiniteResult() {
				positions.add(TopFp.INSTANCE);
			}

			@Override
			public void processFiniteResult(Set<Type> possibleSuperclassTypes) {

				for (Type supertype : possibleSuperclassTypes) {

					if (supertype instanceof TClass) {
						if (!doneClasses.contains(supertype)) {
							TClass superclass = (TClass) supertype;
							flowToMethodsOfClass(superclass, doneMethods,
									doneClasses, goalManager, positions);
						} else {
							System.err.println("UNTYPABLE: " + classObject
									+ " appears to inherit circularly");
						}
					} else {
						System.err.println("UNTYPABLE: " + classObject
								+ " appears to inherit from non-class "
								+ superclass);
					}
				}
			}
		});
	}

	/**
	 * Returns the object(s) bound to the __init__ member of the class.
	 * 
	 * These are <em>not bound</em> to an instance of the class, they are the
	 * unbound methods.
	 */
	private Result<Type> initMethodTypes(SubgoalManager goalManager) {
		return memberType("__init__", goalManager);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((classObject == null) ? 0 : classObject.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TClass other = (TClass) obj;
		if (classObject == null) {
			if (other.classObject != null)
				return false;
		} else if (!classObject.equals(other.classObject))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TClass [" + getName() + "]";
	}

}

final class InitCodeObjectFinder implements
		Transformer<Type, Result<InvokableCodeObject>> {

	private final SubgoalManager goalManager;

	InitCodeObjectFinder(SubgoalManager goalManager) {
		this.goalManager = goalManager;
	}

	@Override
	public Result<InvokableCodeObject> transformFiniteResult(
			Set<Type> initImplementations) {

		RedundancyEliminator<InvokableCodeObject> codeObjects = new RedundancyEliminator<InvokableCodeObject>();

		for (Type initType : initImplementations) {

			/*
			 * The __init__ method can be implemented by any callable object,
			 * plain function, callable object, lambda, even another class
			 * (though that causes an error _after_ calling that class's
			 * constructor because __init__ implementation are expected to
			 * return None). This could be an arbitrarily long chain of
			 * callables before we get to a code object so we just delegate
			 * lookup to the next callable.
			 */

			if (initType instanceof TCallable) {

				codeObjects.add(((TCallable) initType)
						.codeObjectsInvokedByCall(goalManager));

			} else {
				System.err.println("UNTYPABLE: __init__ member "
						+ "appears to be implemented by a non-callable: "
						+ initType);
				return TopI.INSTANCE;
			}

			if (codeObjects.isFinished()) {
				break;
			}
		}

		return codeObjects.result();
	}

	@Override
	public Result<InvokableCodeObject> transformInfiniteResult() {
		return TopI.INSTANCE;
	}

}
