package uk.ac.ic.doc.gander.flowinference.dda;

/**
 * Mechanism via which a {@link Goal} can manage the {@link Goal}s it depends
 * upon.
 */
interface SubgoalManager {

	void registerSubgoal(Goal newGoal);

	Object currentSolutionOfGoal(Goal subgoal);

}
