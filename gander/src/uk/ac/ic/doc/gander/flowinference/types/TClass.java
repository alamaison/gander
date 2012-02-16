package uk.ac.ic.doc.gander.flowinference.types;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.Argument;
import uk.ac.ic.doc.gander.flowinference.ArgumentPassage;
import uk.ac.ic.doc.gander.flowinference.TopI;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopFp;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopP;
import uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow.ExpressionPosition;
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
import uk.ac.ic.doc.gander.model.OldNamespace;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.FormalParameter;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;

public class TClass implements TCodeObject, TCallable {

	private final class ReceivingParameterFinder implements
			Transformer<Type, Result<ArgumentPassage>> {

		private final Argument argument;

		public ReceivingParameterFinder(Argument argument) {
			this.argument = argument;
		}

		@Override
		public Result<ArgumentPassage> transformFiniteResult(
				Set<Type> initImplementations) {

			Set<ArgumentPassage> parameters = new HashSet<ArgumentPassage>();

			for (Type initType : initImplementations) {
				if (initType instanceof TCodeObject) {
					CodeObject codeObject = ((TCodeObject) initType)
							.codeObject();

					if (codeObject instanceof InvokableCodeObject) {

						parameters.add(argument.passArgumentAtCall(
								(InvokableCodeObject) codeObject,
								new MethodStylePassingStrategy()));

					} else {
						System.err.println("UNTYPABLE: __init__ "
								+ "implementation not invokable. class: "
								+ classObject + " init implementation: "
								+ initType);
						return TopP.INSTANCE;
					}
				} else {
					// TODO: init might be a callable object
					return TopP.INSTANCE;
				}
			}

			return new FiniteResult<ArgumentPassage>(parameters);
		}

		@Override
		public Result<ArgumentPassage> transformInfiniteResult() {
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

	public String getName() {
		return getClassInstance().getFullName();
	}

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
	public Result<Type> memberType(String memberName, SubgoalManager goalManager) {

		NamespaceName member = new NamespaceName(memberName,
				classObject.fullyQualifiedNamespace());
		return goalManager.registerSubgoal(new NamespaceNameTypeGoal(member));
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<OldNamespace> memberReadableNamespaces() {
		return Collections.<OldNamespace> singleton(classObject
				.fullyQualifiedNamespace());
	}

	/**
	 * {@inheritDoc}
	 */
	public OldNamespace memberWriteableNamespace() {
		return classObject.fullyQualifiedNamespace();
	}

	@Override
	public Result<Type> typeOfArgumentPassedToParameter(
			FormalParameter parameter, ModelSite<Call> callSite,
			SubgoalManager goalManager) {

		Result<Type> initMethodTypes = initMethodTypes(goalManager);

		return initMethodTypes.transformResult(new ParameterTyper(parameter,
				callSite, goalManager));
	}

	private class ParameterTyper implements Transformer<Type, Result<Type>> {

		private final FormalParameter parameter;
		private final ModelSite<Call> callSite;
		private final SubgoalManager goalManager;

		ParameterTyper(FormalParameter parameter, ModelSite<Call> callSite,
				SubgoalManager goalManager) {
			this.parameter = parameter;
			this.callSite = callSite;
			this.goalManager = goalManager;
		}

		public Result<Type> transformFiniteResult(Set<Type> result) {
			RedundancyEliminator<Type> parameterType = new RedundancyEliminator<Type>();

			for (Type initType : result) {
				if (parameterType.isFinished()) {
					break;
				}

				if (initType instanceof TFunction) {
					TBoundMethod init = new TBoundMethod(
							((TFunction) initType).codeObject(), new TObject(
									classObject));
					parameterType.add(init.typeOfArgumentPassedToParameter(
							parameter, callSite, goalManager));
				} else {
					// XXX: init might not be a function?!
					parameterType.add(TopT.INSTANCE);
				}
			}

			return parameterType.result();
		}

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
	 * Ordinal arguments passed to a call to a bound method are passed to the
	 * parameter of the receiver that is one further along the parameter list
	 * than the ordinal.
	 */
	public Result<ArgumentPassage> destinationsReceivingArgument(
			Argument argument, SubgoalManager goalManager) {
		if (argument == null) {
			throw new NullPointerException("Argument is not optional");
		}
		if (goalManager == null) {
			throw new NullPointerException(
					"Goal manager required to resolve constructor");
		}

		Result<Type> initMethodTypes = initMethodTypes(goalManager);

		return initMethodTypes.transformResult(new ReceivingParameterFinder(
				argument));
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
		Result<Type> initObjects = initMethodTypes(goalManager);

		return initObjects.transformResult(new InitCodeObjectFinder());
	}

	private static final class InitCodeObjectFinder implements
			Transformer<Type, Result<InvokableCodeObject>> {

		@Override
		public Result<InvokableCodeObject> transformFiniteResult(
				Set<Type> initImplementations) {

			Set<InvokableCodeObject> codeObjects = new HashSet<InvokableCodeObject>();

			for (Type initType : initImplementations) {
				if (initType instanceof TCodeObject) {
					CodeObject codeObject = ((TCodeObject) initType)
							.codeObject();
					if (codeObject instanceof InvokableCodeObject) {
						codeObjects.add((InvokableCodeObject) codeObject);
					} else {
						// XXX: init might not be a callable?!
						return TopI.INSTANCE;
					}
				} else {
					// TODO: init might be a callable object
					return TopI.INSTANCE;
				}
			}

			return new FiniteResult<InvokableCodeObject>(codeObjects);
		}

		@Override
		public Result<InvokableCodeObject> transformInfiniteResult() {
			return TopI.INSTANCE;
		}

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

		Set<FlowPosition> localPositions = new HashSet<FlowPosition>();
		for (Function method : methods) {
			List<ModelSite<exprType>> parameters = method.codeObject()
					.formalParameters().parameters();

			if (parameters.size() > 0) {
				ModelSite<exprType> selfParameter = parameters.get(0);
				assert selfParameter.codeObject().equals(method.codeObject());
				localPositions.add(new ExpressionPosition(selfParameter));
				doneMethods.add(method.getName());
			} else {
				System.err.println("Method missing self parameter: " + method);
			}
		}

		positions.add(new FiniteResult<FlowPosition>(localPositions));

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

			public void processInfiniteResult() {
				positions.add(TopFp.INSTANCE);
			}

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
