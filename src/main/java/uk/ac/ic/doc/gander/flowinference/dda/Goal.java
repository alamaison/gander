package uk.ac.ic.doc.gander.flowinference.dda;

public interface Goal<T> {

    /**
     * Most optimistic solution to the problem.
     * 
     * This solution can't depend on any on any subgoals so may be wildly
     * incorrect.
     * 
     * @return 'Maximally precise' solution.
     */
    T initialSolution();

    /**
     * Returns the best-guess solution for the goal, given the provisional
     * solutions to subgoals available via the {@link SubgoalManager}.
     * 
     * @param goalManager
     *            Coordinator for subgoals of this goal.
     * @return New, provisional solution.
     */
    T recalculateSolution(SubgoalManager goalManager);

}
