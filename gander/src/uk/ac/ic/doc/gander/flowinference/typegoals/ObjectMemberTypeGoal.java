package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Set;

import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.modelgoals.AttributeAssignmentSitesGoal;
import uk.ac.ic.doc.gander.flowinference.types.TObject;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeConcentrator;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.AssignmentSite;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Model;

/**
 * Infer the type of a member of an object's dictionary for an object that is an
 * instance of the given Class.
 */
final class ObjectMemberTypeGoal implements TypeGoal {

	private final Model model;
	private final Class klass;
	private final String memberName;

	ObjectMemberTypeGoal(Model model, Class klass, String memberName) {
		this.model = model;
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
	public TypeJudgement recalculateSolution(SubgoalManager goalManager) {

		/*
		 * To decide on the type of the member we have to look at all
		 * assignments to an attribute whose target could be the given class.
		 * First we find all attributes with the given name and try to filter
		 * some of them out by inferring their target's type in the hope that it
		 * excludes our given type. If if does, it means we can ignore that
		 * assignment as it can't affect the given class's instances.
		 */
		// XXX: We only look at attributes referenced using a matching name.
		// is this enough? What about fields of modules, for instance (yes I
		// realise these never get here because NamespaceMemberTypeGoal
		// handles them but they are technically objects).
		Set<AssignmentSite<Attribute>> attributeAssignment = goalManager
				.registerSubgoal(new AttributeAssignmentSitesGoal(model,
						memberName));

		TypeConcentrator types = new TypeConcentrator();

		for (AssignmentSite<Attribute> assignment : attributeAssignment) {

			/*
			 * Try to filter out some attributes assignments even though they
			 * match by name.
			 * 
			 * The only assignment to attributes that we can safely ignore are
			 * those whose target container object has been established not to
			 * be an instance of the class in question.
			 */
			TypeJudgement objectType = goalManager
					.registerSubgoal(new ExpressionTypeGoal(model, assignment
							.getEnclosingScope(), assignment.getTarget().value));
			if (typeDefinitelyNotContainerForThisAttribute(objectType)) {
				continue;
			}

			exprType rhs = ((Assign) assignment.getAssignment()).value;
			types.add(goalManager.registerSubgoal(new ExpressionTypeGoal(model,
					assignment.getEnclosingScope(), rhs)));
			if (types.isFinished())
				break;
		}

		/*
		 * An object member may also refer to the member in the metaclass object
		 * so we have to add these types too.
		 */
		TypeJudgement metaClassMemberTypes = goalManager
				.registerSubgoal(new NamespaceMemberTypeGoal(model, klass,
						memberName));
		types.add(metaClassMemberTypes);

		return types.getJudgement();
	}

	private boolean typeDefinitelyNotContainerForThisAttribute(
			TypeJudgement type) {
		if (!(type instanceof SetBasedTypeJudgement)) {
			return false;
		}

		Set<Type> inferredTypes = ((SetBasedTypeJudgement) type)
				.getConstituentTypes();

		return !inferredTypes.contains(new TObject(klass));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((memberName == null) ? 0 : memberName.hashCode());
		result = prime * result + ((klass == null) ? 0 : klass.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
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
		if (memberName == null) {
			if (other.memberName != null)
				return false;
		} else if (!memberName.equals(other.memberName))
			return false;
		if (klass == null) {
			if (other.klass != null)
				return false;
		} else if (!klass.equals(other.klass))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ObjectMemberTypeGoal [memberName=" + memberName + ", klass="
				+ klass + "]";
	}

}
