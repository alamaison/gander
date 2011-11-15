package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ast.AstParentNodeFinder;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.CodeObjectPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.ExpressionPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowGoal;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.Top;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeConcentrator;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelSite;

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
		 * 
		 * We find these by issuing a flow query for the class. Any calls to any
		 * of the places it flows to result in new instances of this class.
		 * 
		 * The type of our member is determined by the values assigned to an
		 * attribute of these instances with the same name so we filter them to
		 * find the places where they are subject to an attribute access which,
		 * itself, is bound to a value.
		 */
		// XXX: We only look at attributes referenced using a matching name.
		// is this enough? What about fields of modules, for instance (yes I
		// realise these never get here because NamespaceMemberTypeGoal
		// handles them but they are technically objects).
		Set<ModelSite<? extends exprType>> classReferences = goalManager
				.registerSubgoal(new FlowGoal(new CodeObjectPosition(klass,
						model)));
		Set<ModelSite<Call>> constructors = new HashSet<ModelSite<Call>>();
		for (ModelSite<? extends exprType> classSite : classReferences) {
			SimpleNode parent = AstParentNodeFinder.findParent(classSite
					.getNode(), classSite.getEnclosingScope().getAst());
			if (parent instanceof Call) {
				constructors.add(new ModelSite<Call>((Call) parent, classSite
						.getEnclosingScope(), model));
			}
		}

		Set<ModelSite<? extends exprType>> objectReferences = new HashSet<ModelSite<? extends exprType>>();
		for (ModelSite<Call> constructor : constructors) {
			Set<ModelSite<? extends exprType>> references = goalManager
					.registerSubgoal(new FlowGoal(new ExpressionPosition<Call>(
							constructor)));
			if (references != null) {
				objectReferences.addAll(references);
			} else {
				/*
				 * We have no idea where the object flowed to so we can't say
				 * what type the member might have.
				 */
				return new Top();
			}
		}

		/*
		 * Collect the expressions that access our named member on an instance
		 * of our class.
		 */
		Set<ModelSite<Attribute>> memberAccesses = new HashSet<ModelSite<Attribute>>();
		for (ModelSite<? extends exprType> object : objectReferences) {

			SimpleNode parent = AstParentNodeFinder.findParent(
					object.getNode(), object.getEnclosingScope().getAst());
			if (parent instanceof Attribute) {
				if (((NameTok) ((Attribute) parent).attr).id.equals(memberName)) {
					memberAccesses.add(new ModelSite<Attribute>(
							(Attribute) parent, object.getEnclosingScope(),
							model));
				}
			}
		}

		TypeConcentrator types = new TypeConcentrator();

		for (ModelSite<Attribute> accessSite : memberAccesses) {

			SimpleNode parent = AstParentNodeFinder.findParent(accessSite
					.getNode(), accessSite.getEnclosingScope().getAst());

			// Check that attribute is being bound by assignment
			// FIXME: Attributes can be bound by any of the binding statements
			if (parent instanceof Assign
					&& Arrays.asList(((Assign) parent).targets).contains(
							accessSite.getNode())) {

				ModelSite<exprType> rhs = new ModelSite<exprType>(
						((Assign) parent).value,
						accessSite.getEnclosingScope(), accessSite.getModel());
				types.add(goalManager.registerSubgoal(new ExpressionTypeGoal(
						rhs)));
				if (types.isFinished())
					break;
			}
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
