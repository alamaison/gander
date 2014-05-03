package uk.ac.ic.doc.gander.flowinference.dda;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class KnowledgeBase {
    private final Map<Goal<?>, Object> solutionStore = new HashMap<Goal<?>, Object>();
    private final DependencyGraph dependencies = new HashDependencyGraph();

    /**
     * Add a goal and establish a dependency on an existing goal.
     * 
     * If the parent goal doesn't already exist in the knowledgebase, the
     * behaviour is undefined but is likely to succeed silently but make {@code
     * getLastSolution} later return the incorrect solution, namely {@code null}
     * for this goal.
     */
    boolean addGoal(Goal<?> goal, Goal<?> parent) {
        assert solutionStore.containsKey(parent);
        dependencies.addDependency(parent, goal);
        return addGoal(goal);
    }

    /**
     * Add a goal without establishing any new dependencies.
     * 
     * It may or may not be a root goal (one that doesn't depend on anything -
     * there can be multiple root goals). When reusing the knowledgebase, it may
     * be added as a root goal but have existing dependencies in the
     * knowledgebase.
     */
    boolean addGoal(Goal<?> goal) {
        if (!solutionStore.containsKey(goal)) {
            solutionStore.put(goal, goal.initialSolution());
            return true;
        } else {
            return false;
        }
    }

    void updateSolution(Goal<?> goal, Object newSolution) {
        assert solutionStore.containsKey(goal);
        solutionStore.put(goal, newSolution);
    }

    Object getLastSolution(Goal<?> goal) {
        assert solutionStore.containsKey(goal);
        return solutionStore.get(goal);
    }

    Collection<? extends Goal<?>> dependents(Goal<?> goal) {
        return dependencies.dependents(goal);
    }
}

/**
 * Representation of dependencies between goals.
 */
interface DependencyGraph {

    Collection<? extends Goal<?>> dependents(Goal<?> goal);

    void addDependency(Goal<?> parent, Goal<?> subgoal);

}


/**
 * Representation of dependencies between goals implemented using hash maps.
 * 
 * The constructor doesn't take an initial goal because this doesn't belong in a
 * <em>dependency</em> graph. The initial goal, be definition, has no
 * dependencies so insisting on being created with such a goal puts unnecessary
 * requirements on the caller.
 */
final class HashDependencyGraph implements DependencyGraph {
    private final HashMap<Goal<?>, Set<Goal<?>>> graph = new HashMap<Goal<?>, Set<Goal<?>>>();

    public Collection<? extends Goal<?>> dependents(Goal<?> goal) {
        Set<Goal<?>> c = graph.get(goal);
        if (c == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableCollection(graph.get(goal));
        }
    }

    public void addDependency(Goal<?> parent, Goal<?> subgoal) {
        Set<Goal<?>> dependents = graph.get(subgoal);
        if (dependents == null) {
            dependents = new HashSet<Goal<?>>();
            graph.put(subgoal, dependents);
        }

        dependents.add(parent);
    }
}
