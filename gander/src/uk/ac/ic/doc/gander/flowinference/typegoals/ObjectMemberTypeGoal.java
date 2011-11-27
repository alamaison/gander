package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Set;

import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.CodeObjectNamespacePosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowGoal;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.Top;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeConcentrator;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.NamespaceName;

/**
 * Infer the type of a member of an object's dictionary for an object that is an
 * instance of the given Class.
 */
final class ObjectMemberTypeGoal implements TypeGoal {

	private final Class klass;
	private final String memberName;

	ObjectMemberTypeGoal(Class klass, String memberName) {
		this.klass = klass;
		this.memberName = memberName;
	}

	public TypeJudgement initialSolution() {
		return SetBasedTypeJudgement.BOTTOM;
	}

	/**
	 * The type of an object's member is the union of the types assigned to that
	 * name. Because we don't track individual instances in this
	 * container-insensitive analysis, this is summarised to become the union of
	 * the types assigned to that member of any instance of the same class.
	 */
	public TypeJudgement recalculateSolution(final SubgoalManager goalManager) {

		/*
		 * To decide on the type of the member we have to look at all
		 * assignments to an attribute whose target could be the given class or
		 * an instance thereof.
		 * 
		 * The type of our member is determined by the values assigned to an
		 * attribute of these instances with the same name so we filter them to
		 * find the places where they are subject to an attribute access which,
		 * itself, is bound to a value.
		 */

		Set<ModelSite<? extends exprType>> namespaceReferences = goalManager
				.registerSubgoal(new FlowGoal(new CodeObjectNamespacePosition(
						klass)));
		if (namespaceReferences == null) {
			/*
			 * We have no idea where the namespace flowed to so we can't say
			 * what type the member might have.
			 */
			return new Top();
		}

		/*
		 * Collect the expressions that access our named member on an instance
		 * of our class.
		 */
		// XXX: We only look at attributes referenced using a matching name.
		// is this enough? What about fields of modules, for instance (yes I
		// realise these never get here because NamespaceNameTypeGoal
		// handles them but they are technically objects).
		Set<ModelSite<Attribute>> memberAccesses = new NamedAttributeAccessFinder(
				namespaceReferences, memberName).accesses();

		final TypeConcentrator types = new TypeConcentrator();

		Set<Type> attributeSummary = new AttributeTypeSummariser(
				memberAccesses, goalManager).type();
		if (attributeSummary != null)
			types.add(new SetBasedTypeJudgement(attributeSummary));
		else
			return new Top();

		/*
		 * An object member may also refer to the member in the metaclass object
		 * so we have to add these types too.
		 */
		TypeJudgement metaClassMemberTypes = goalManager
				.registerSubgoal(new NamespaceNameTypeGoal(new NamespaceName(
						memberName, klass)));
		types.add(metaClassMemberTypes);

		return types.getJudgement();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((klass == null) ? 0 : klass.hashCode());
		result = prime * result
				+ ((memberName == null) ? 0 : memberName.hashCode());
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
		ObjectMemberTypeGoal other = (ObjectMemberTypeGoal) obj;
		if (klass == null) {
			if (other.klass != null)
				return false;
		} else if (!klass.equals(other.klass))
			return false;
		if (memberName == null) {
			if (other.memberName != null)
				return false;
		} else if (!memberName.equals(other.memberName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ObjectMemberTypeGoal [memberName=" + memberName + ", klass="
				+ klass + "]";
	}

}
