package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Set;

import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.NameTok;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.TNamespace;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.Top;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Member;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;

final class AttributeTypeGoal implements TypeGoal {

	private final Model model;
	private final Namespace scope;
	private final Attribute attribute;

	AttributeTypeGoal(Model model, Namespace scope, Attribute attribute) {
		this.model = model;
		this.scope = scope;
		this.attribute = attribute;
	}

	public TypeJudgement initialSolution() {
		return SetBasedTypeJudgement.BOTTOM;
	}

	public TypeJudgement recalculateSolution(SubgoalManager goalManager) {
		ExpressionTypeGoal typer = new ExpressionTypeGoal(model, scope,
				attribute.value);
		TypeJudgement targetTypes = goalManager.registerSubgoal(typer);
		if (targetTypes instanceof SetBasedTypeJudgement) {
			Set<Type> types = ((SetBasedTypeJudgement) targetTypes)
					.getConstituentTypes();

			if (types.size() == 1) {
				Type t = types.iterator().next();
				if (t instanceof TNamespace) {

					/*
					 * TODO: If we can't find a matching entry in the namespace
					 * type definition then we will have to do something more
					 * complex to deal with the possibility that it is a field.
					 * Or even worse, that it is a method added at runtime. For
					 * the moment we return Top (don't know) in that case.
					 */
					return extractTokenTypeFromNamespace(model,
							((TNamespace) t).getNamespaceInstance(),
							((NameTok) attribute.attr).id, goalManager);
				}

			}
		}
		return new Top();
	}

	private static TypeJudgement extractTokenTypeFromNamespace(Model model,
			Namespace scope, String name, SubgoalManager goalManager) {
		Member member = scope.lookupMember(name);
		if (member != null) {
			return convertMemberToType(member);
		} else {
			NameTypeGoal typer = new NameTypeGoal(model, scope, name);
			return goalManager.registerSubgoal(typer);
		}
	}

	private static TypeJudgement convertMemberToType(Member member) {
		if (member instanceof Module) {
			return new SetBasedTypeJudgement(new TModule((Module) member));
		} else if (member instanceof Class) {
			return new SetBasedTypeJudgement(new TClass((Class) member));
		} else if (member instanceof Function) {
			return new SetBasedTypeJudgement(new TFunction((Function) member));
		} else {
			return new Top();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attribute == null) ? 0 : attribute.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof AttributeTypeGoal))
			return false;
		AttributeTypeGoal other = (AttributeTypeGoal) obj;
		if (attribute == null) {
			if (other.attribute != null)
				return false;
		} else if (!attribute.equals(other.attribute))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		return true;
	}
}
