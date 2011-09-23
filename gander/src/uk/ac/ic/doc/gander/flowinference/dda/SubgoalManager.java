package uk.ac.ic.doc.gander.flowinference.dda;

/**
 * Mechanism via which a {@link Goal} can manage the {@link Goal}s it depends
 * upon.
 */
public interface SubgoalManager {

	/**
	 * Register goal with goal solver and return its current best solution.
	 * 
	 * @param subgoal
	 *            Subgoal that may or may not have a logically-equal instance
	 *            already registered with the system.
	 * @return The current best solution for a goal equal to the one specified.
	 */
	Object registerSubgoal(Goal subgoal);

}
