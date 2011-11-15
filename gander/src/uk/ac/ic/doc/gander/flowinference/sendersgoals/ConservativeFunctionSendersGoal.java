package uk.ac.ic.doc.gander.flowinference.sendersgoals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.typegoals.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.sitefinders.CardinalCallSitesFinder;

public class ConservativeFunctionSendersGoal implements SendersGoal {
	private final Function callable;
	private final Model model;

	// not significant for identity - derived from model
	private Set<ModelSite<Call>> candidateCallSites = null;

	public ConservativeFunctionSendersGoal(Model model, Function callable) {
		this.model = model;
		this.callable = callable;
	}

	public Set<ModelSite<Call>> initialSolution() {
		return Collections.emptySet();
	}

	public Set<ModelSite<Call>> recalculateSolution(
			final SubgoalManager goalManager) {
		if (candidateCallSites == null) {

			candidateCallSites = new CardinalCallSitesFinder(model, callable
					.getAst().args.args.length).getSites();

			/*
			 * // FIXME: HACK: // Filter out any callables not called by
			 * matching name HashSet<ModelSite<Call>> filteredCandidates = new
			 * HashSet<ModelSite<Call>>(); for (ModelSite<Call> callSite :
			 * candidateCallSites) { if (callSite.getNode().func instanceof Name
			 * && ((Name) callSite.getNode().func).id.equals(callable
			 * .getName())) { filteredCandidates.add(callSite); } }
			 * candidateCallSites = filteredCandidates;
			 */
		}

		Set<ModelSite<Call>> sites = new HashSet<ModelSite<Call>>();
		for (ModelSite<Call> callSite : candidateCallSites) {

			ModelSite<exprType> callableExpression = new ModelSite<exprType>(
					callSite.getNode().func, callSite.getEnclosingScope(),
					callSite.getModel());
			TypeJudgement type = goalManager
					.registerSubgoal(new ExpressionTypeGoal(callableExpression));

			/*
			 * The only call sites that _shouldn't_ be included in the result
			 * are those that guarantee they never call the function in
			 * questions.
			 */
			if (type instanceof SetBasedTypeJudgement) {
				Set<Type> targetTypes = ((SetBasedTypeJudgement) type)
						.getConstituentTypes();
				if (!targetTypes.contains(new TFunction(callable)))
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
		result = prime * result
				+ ((callable == null) ? 0 : callable.hashCode());
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
		ConservativeFunctionSendersGoal other = (ConservativeFunctionSendersGoal) obj;
		if (callable == null) {
			if (other.callable != null)
				return false;
		} else if (!callable.equals(other.callable))
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
		return "ConservativeFunctionSendersGoal [callable=" + callable + "]";
	}

}
