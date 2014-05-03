package uk.ac.ic.doc.gander.flowinference.abstractmachine;

import java.util.Collections;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.Namespace;
import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.NullArgument;
import uk.ac.ic.doc.gander.flowinference.argument.SelfArgument;
import uk.ac.ic.doc.gander.flowinference.call.CallDispatch;
import uk.ac.ic.doc.gander.flowinference.call.TopD;
import uk.ac.ic.doc.gander.flowinference.callframe.CallSiteStackFrame;
import uk.ac.ic.doc.gander.flowinference.callframe.StackFrame;
import uk.ac.ic.doc.gander.flowinference.callframe.StrategyBasedStackFrame;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow.ArgumentFlower;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.typegoals.namespacename.NamespaceNameTypeGoal;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;

/**
 * Abstract model of Python class objects.
 */
public class PyClass implements PyCodeObject, PyCallable {

	private final ClassCO classObject;

	public PyClass(ClassCO classInstance) {
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
	public PyClass(Class classInstance) {
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
	public Result<PyObject> returnType(SubgoalManager goalManager) {
		/*
		 * Calling a class is a constructor call. Constructors are special
		 * functions so we can infer the return type immediately. It is an
		 * instance of the class being called.
		 */
		return new FiniteResult<PyObject>(Collections.singleton(new PyInstance(
				classObject)));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Members on a class are returned directly from the class namespace.
	 */
	@Override
	public Result<PyObject> memberType(String memberName, SubgoalManager goalManager) {

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

	/**
	 * {@inheritDoc}
	 * 
	 * A call made on a class object is passed on to any of the objects assigned
	 * to the {@code __init__} member of the class's namespace.
	 */
	@Override
	public Result<CallDispatch> dispatches(StackFrame<Argument> callFrame,
			SubgoalManager goalManager) {
		if (callFrame == null)
			throw new NullPointerException("Call frame required");
		if (goalManager == null)
			throw new NullPointerException("Goal manager required");

		Result<PyObject> initObjects = initMemberTypes(goalManager);

		return initObjects.transformResult(new InitDispatchFinder(callFrame,
				classObject, goalManager));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * We model this for results of constructor calls by flowing the value to
	 * the positions of the {@code self} parameter in the __init__ member.
	 */
	@Override
	public Result<FlowPosition> flowPositionsCausedByCalling(
			ModelSite<Call> syntacticCallSite, SubgoalManager goalManager) {

		StackFrame<Argument> stackFrame = new CallSiteStackFrame(
				syntacticCallSite);

		Result<CallDispatch> calls = dispatches(stackFrame, goalManager);

		return calls.transformResult(new ArgumentFlower(
				constructorSelfArgument(), goalManager));
	}

	/**
	 * Returns the object(s) bound to the __init__ member of the class.
	 * 
	 * These are <em>not bound</em> to an instance of the class, they are the
	 * unbound methods.
	 */
	private Result<PyObject> initMemberTypes(SubgoalManager goalManager) {
		return memberType("__init__", goalManager);
	}

	@Override
	public Argument selfArgument() {
		return NullArgument.INSTANCE;
	}

	private Argument constructorSelfArgument() {
		return new SelfArgument(0, new PyInstance(classObject));
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
		PyClass other = (PyClass) obj;
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

final class InitDispatchFinder implements
		Transformer<PyObject, Result<CallDispatch>> {

	private final SubgoalManager goalManager;
	private final StackFrame<Argument> callFrame;
	private final ClassCO classObject;

	InitDispatchFinder(StackFrame<Argument> callFrame, ClassCO classObject,
			SubgoalManager goalManager) {
		this.callFrame = callFrame;
		this.classObject = classObject;
		this.goalManager = goalManager;
	}

	@Override
	public Result<CallDispatch> transformFiniteResult(
			Set<PyObject> initImplementations) {

		RedundancyEliminator<CallDispatch> dispatches = new RedundancyEliminator<CallDispatch>();

		for (PyObject initType : initImplementations) {

			/*
			 * The __init__ method can be implemented by any callable object,
			 * plain function, callable object, lambda, even another class
			 * (though that causes an error _after_ calling that class's
			 * constructor because __init__ implementation are expected to
			 * return None). This could be an arbitrarily long chain of
			 * callables before we get to a code object so we just delegate
			 * lookup to the next callable.
			 */

			if (initType instanceof PyCallable) {

				/*
				 * The __init__ object is not a bound method (at least, not
				 * bound to us) so we bind it here.
				 * 
				 * FIXME: This doesn't quite work like Python if the __init__
				 * implementation was anything other than a plain function
				 */
				StackFrame<Argument> constructorCall = new StrategyBasedStackFrame(
						callFrame, new MethodStylePassingStrategy(new PyInstance(
								classObject)));

				dispatches.add(((PyCallable) initType).dispatches(
						constructorCall, goalManager));

			} else {
				System.err.println("UNTYPABLE: __init__ member "
						+ "appears to be implemented by a non-callable: "
						+ initType);
				return TopD.INSTANCE;
			}

			if (dispatches.isFinished()) {
				break;
			}
		}

		return dispatches.result();
	}

	@Override
	public Result<CallDispatch> transformInfiniteResult() {
		return TopD.INSTANCE;
	}

}
