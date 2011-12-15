package uk.ac.ic.doc.gander.flowinference.types;

import java.util.Collections;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.typegoals.NamespaceNameTypeGoal;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;

public class TClass implements TCodeObject, TCallable {

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

	public int passedArgumentOffset() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Members on a class are returned directly from the class namespace.
	 */
	public Result<Type> memberType(String memberName, SubgoalManager goalManager) {

		NamespaceName member = new NamespaceName(memberName, classObject
				.fullyQualifiedNamespace());
		return goalManager.registerSubgoal(new NamespaceNameTypeGoal(member));
	}

	public Result<Type> typeOfArgumentAtNamedParameter(
			final String parameterName, final ModelSite<Call> callSite,
			final SubgoalManager goalManager) {

		Result<Type> initMethodTypes = memberType("__init__", goalManager);
		return initMethodTypes
				.transformResult(new Transformer<Type, Result<Type>>() {

					public Result<Type> transformFiniteResult(Set<Type> result) {
						RedundancyEliminator<Type> parameterType = new RedundancyEliminator<Type>();

						for (Type initType : result) {
							if (initType instanceof TFunction) {
								TBoundMethod init = new TBoundMethod(
										((TFunction) initType).codeObject(),
										new TObject(classObject));
								parameterType.add(init
										.typeOfArgumentAtNamedParameter(
												parameterName, callSite,
												goalManager));
							} else {
								// XXX: init might not be a function?!
								parameterType.add(TopT.INSTANCE);
							}
						}

						return parameterType.result();
					}

					public Result<Type> transformInfiniteResult() {
						/*
						 * Can't work out what __init__ implementations could be
						 * called so we certainly can't work out the type of the
						 * self parameter
						 */
						return TopT.INSTANCE;
					}
				});
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
