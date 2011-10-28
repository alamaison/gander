package uk.ac.ic.doc.gander.flowinference.modelgoals;

import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;

import uk.ac.ic.doc.gander.flowinference.dda.Goal;
import uk.ac.ic.doc.gander.model.AssignmentSite;

public interface AssignmentGoal<T extends SimpleNode> extends
		Goal<Set<AssignmentSite<T>>> {

}
