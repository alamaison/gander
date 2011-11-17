package uk.ac.ic.doc.gander.flowinference.sendersgoals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.modelgoals.CallSitesGoal;
import uk.ac.ic.doc.gander.flowinference.modelgoals.CallSitesGoal.Predicate;
import uk.ac.ic.doc.gander.flowinference.typegoals.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.ModelSite;

public final class MethodSendersGoal implements SendersGoal {

	private static final class MatchingMethodName implements Predicate {

		private final String name;

		MatchingMethodName(String name) {
			assert name != null;
			this.name = name;
		}

		public boolean isMatch(ModelSite<Call> callSite) {
			if (!(callSite.astNode().func instanceof Attribute))
				return false;

			String selector = ((NameTok) ((Attribute) callSite.astNode().func).attr).id;
			return name.equals(selector);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
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
			MatchingMethodName other = (MatchingMethodName) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "MatchingMethodName [name=" + name + "]";
		}

	}

	private final Function method;

	public MethodSendersGoal(Function method) {
		assert method.getParentScope() instanceof Class;
		this.method = method;
	}

	public Set<ModelSite<Call>> initialSolution() {
		return Collections.emptySet();
	}

	public Set<ModelSite<Call>> recalculateSolution(SubgoalManager goalManager) {
		Set<ModelSite<Call>> candidateSenders = goalManager
				.registerSubgoal(new CallSitesGoal(method.model(),
						new MatchingMethodName(method.getName())));

		Set<ModelSite<Call>> sites = new HashSet<ModelSite<Call>>();
		for (ModelSite<Call> callSite : candidateSenders) {

			ModelSite<exprType> callable = new ModelSite<exprType>(callSite
					.astNode().func, callSite.codeObject());
			TypeJudgement type = goalManager
					.registerSubgoal(new ExpressionTypeGoal(callable));

			/*
			 * The only call sites that _shouldn't_ be included in the result
			 * are those that guarantee they never call send to the receiver in
			 * question.
			 */
			if (type instanceof SetBasedTypeJudgement) {
				Set<Type> receiverTypes = ((SetBasedTypeJudgement) type)
						.getConstituentTypes();
				if (!receiverTypes.contains(new TFunction(method)))
					continue; // not this call site
			}

			sites.add(callSite);
		}

		return sites;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
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
		MethodSendersGoal other = (MethodSendersGoal) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MethodSendersGoal [method=" + method + "]";
	}

}
