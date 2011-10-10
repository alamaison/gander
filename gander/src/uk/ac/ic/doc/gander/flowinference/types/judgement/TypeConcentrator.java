package uk.ac.ic.doc.gander.flowinference.types.judgement;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.types.Type;

/**
 * Produces the union of the type judgements added to it.
 * 
 * {@link Top} trumps all.
 */
public final class TypeConcentrator {

	private TypeJudgement compoundJudgement = SetBasedTypeJudgement.BOTTOM;

	public void add(final TypeJudgement judgement) {
		assert judgement != null;

		/**
		 * XXX: Effectively a switch on type tags. Do this better.
		 */
		if (compoundJudgement instanceof Top) {
			return;
		} else if (judgement instanceof Top) {
			compoundJudgement = judgement;
		} else {
			if (compoundJudgement instanceof SetBasedTypeJudgement
					&& judgement instanceof SetBasedTypeJudgement) {
				Set<Type> union = new HashSet<Type>(
						((SetBasedTypeJudgement) compoundJudgement)
								.getConstituentTypes());
				union.addAll(((SetBasedTypeJudgement) judgement)
						.getConstituentTypes());
				compoundJudgement = new SetBasedTypeJudgement(union);
			} else {
				throw new AssertionError(
						"Unrecognised type judgement: judgement=[" + judgement
								+ "], compoundJudgement=[" + compoundJudgement
								+ "]");
			}
		}
	}

	public TypeJudgement getJudgement() {
		return compoundJudgement;
	}
}
