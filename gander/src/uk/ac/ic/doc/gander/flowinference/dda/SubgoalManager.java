package uk.ac.ic.doc.gander.flowinference.dda;

/**
 * Mechanism via which a {@link Goal} can manage the {@link Goal}s it depends
 * upon.
 */
public interface SubgoalManager {

	Object registerSubgoal(Goal newGoal);

	Object currentSolutionOfGoal(Goal subgoal);

}
