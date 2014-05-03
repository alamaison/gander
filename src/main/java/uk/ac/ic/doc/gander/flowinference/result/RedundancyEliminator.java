package uk.ac.ic.doc.gander.flowinference.result;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.result.Result.Processor;

/**
 * Produces the union of the results added to it.
 * 
 * Adding any non-finite type judgement set the result to {@link Top}. At the
 * moment, that is the same thing as saying, adding top immediately sets the
 * result to {@link Top} which trumps all.
 */
public final class RedundancyEliminator<T> {

    private Result<T> compoundResult = new FiniteResult<T>(Collections
            .<T> emptySet());

    public void add(final Result<T> result) {
        assert result != null;

        if (isFinished()) {
            return;
        } else {

            result.actOnResult(new Processor<T>() {

                public void processFiniteResult(Set<T> results) {
                    // cast is safe because we checked that we weren't finished
                    Set<T> union = new HashSet<T>(
                            (FiniteResult<T>) compoundResult);
                    union.addAll(results);
                    compoundResult = new FiniteResult<T>(union);
                }

                public void processInfiniteResult() {
                    compoundResult = result;
                }
            });
        }
    }

    public Result<T> result() {
        return compoundResult;
    }

    public boolean isFinished() {
        final boolean isFinished[] = { false };

        compoundResult.actOnResult(new Processor<T>() {

            public void processFiniteResult(Set<T> result) {
                isFinished[0] = false;
            }

            public void processInfiniteResult() {
                isFinished[0] = true;
            }
        });

        return isFinished[0];
    }

    @Override
    public String toString() {
        return "RedundancyEliminator[" + compoundResult + "]";
    }

}