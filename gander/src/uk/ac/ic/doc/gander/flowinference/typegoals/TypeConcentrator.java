package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.types.Type;

/**
 * Produces the union of the type judgements added to it.
 * 
 * Adding any non-finite type judgement set the result to {@link Top}. At the
 * moment, that is the same thing as saying, adding top immediately sets the
 * result to {@link Top} which trumps all.
 */
final class TypeConcentrator {

	private TypeJudgement compoundJudgement = SetBasedTypeJudgement.BOTTOM;

	void add(TypeJudgement judgement) {
		assert judgement != null;

		if (!(compoundJudgement instanceof FiniteTypeJudgement)) {
			return;
		} else if (!(judgement instanceof FiniteTypeJudgement)) {
			compoundJudgement = judgement;
		} else {
			Set<Type> union = new HashSet<Type>(
					(FiniteTypeJudgement) compoundJudgement);
			union.addAll((FiniteTypeJudgement) judgement);
			compoundJudgement = new SetBasedTypeJudgement(union);
		}
	}

	TypeJudgement getJudgement() {
		if (isFinished())
			return Top.INSTANCE;
		else
			return compoundJudgement;
	}

	boolean isFinished() {
		return !(compoundJudgement instanceof FiniteTypeJudgement);
	}
}
