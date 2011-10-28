package uk.ac.ic.doc.gander.flowinference.sendersgoals;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.modelgoals.ModelGoal;

/**
 * Goal whose solution is the set of model sites that make a call matching the
 * goal's criteria.
 */
public interface SendersGoal extends ModelGoal<Call> {

}
