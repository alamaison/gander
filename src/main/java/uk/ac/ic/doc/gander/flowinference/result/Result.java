package uk.ac.ic.doc.gander.flowinference.result;

import java.util.Set;

/**
 * Represents the locations an expression can flow to.
 */
public interface Result<D> {

    public interface Processor<I> {

        void processFiniteResult(Set<I> result);

        void processInfiniteResult();
    }

    public interface Transformer<I, R> {

        R transformFiniteResult(Set<I> result);

        R transformInfiniteResult();
    }

    /**
     * Processing this result.
     * 
     * @param action
     *            the processor that decides what to do with each datum in this
     *            result and the infinite case
     */
    void actOnResult(Processor<D> action);

    /**
     * Transform this result into a new result.
     * 
     * @param <R>
     *            the type of the new result
     * 
     * @param action
     *            the transformer that morphs the data into a new result
     */
    <R> R transformResult(Transformer<D, R> action);

}
