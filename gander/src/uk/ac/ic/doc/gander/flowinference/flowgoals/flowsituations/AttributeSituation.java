package uk.ac.ic.doc.gander.flowinference.flowgoals.flowsituations;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.ExpressionPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.typegoals.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * Model of values flowing from the RHS of an attribute access to the {@code
 * self} parameter of methods.
 */
final class AttributeSituation implements FlowSituation {

	private final ModelSite<Attribute> attribute;

	AttributeSituation(ModelSite<Attribute> expression) {
		this.attribute = expression;
	}

	/**
	 * Accessing an attribute can cause the value of the RHS to flow elsewhere
	 * if the attribute is a method.
	 * 
	 * In this case, the attribute's RHS (the receiver) flows to the first
	 * parameter of the method.
	 * 
	 * XXX: Technically, this flow only happens if the attribute is later
	 * called. How do we model this?
	 */
	public Set<FlowPosition> nextFlowPositions(SubgoalManager goalManager) {

		TypeJudgement attributeTypes = goalManager
				.registerSubgoal(new ExpressionTypeGoal(attribute));

		if (attributeTypes instanceof SetBasedTypeJudgement) {

			Set<Function> methods = getMethods((SetBasedTypeJudgement) attributeTypes);

			Set<FlowPosition> selfParameters = new HashSet<FlowPosition>();
			for (Function method : methods) {

				exprType[] parameters = method.getAst().args.args;
				if (parameters.length > 0) {
					selfParameters.add(new ExpressionPosition<Name>(
							new ModelSite<Name>((Name) parameters[0], method,
									attribute.model())));
				} else {
					// method has no parameters!
				}
			}

			return selfParameters;
		} else {
			/*
			 * We have no idea if this attribute is a method so we don't know if
			 * the attribute's RHS flows to any method parameters. Return null
			 * to indicate this (Top).
			 */
			return null;
		}
	}

	/**
	 * Returns any methods in the type judgement.
	 */
	private Set<Function> getMethods(SetBasedTypeJudgement judgement) {

		Set<Function> methods = new HashSet<Function>();

		for (Type attributeType : judgement.getConstituentTypes()) {
			Function method = getIfIsMethod(attributeType);
			if (method != null) {
				methods.add(method);
			}
		}

		return methods;
	}

	/**
	 * Return method if candidate type is a method type.
	 * 
	 * @param candidate
	 *            the type being investigated and transformed.
	 * @return the method represented by the the type if it is a method or
	 *         {@code null} otherwise
	 */
	private Function getIfIsMethod(Type candidate) {
		if (candidate instanceof TFunction) {
			Function method = ((TFunction) candidate).getFunctionInstance();
			if (method.getParentScope() instanceof Class) {
				return method;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attribute == null) ? 0 : attribute.hashCode());
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
		AttributeSituation other = (AttributeSituation) obj;
		if (attribute == null) {
			if (other.attribute != null)
				return false;
		} else if (!attribute.equals(other.attribute))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AttributeSituation [attribute=" + attribute + "]";
	}

}
