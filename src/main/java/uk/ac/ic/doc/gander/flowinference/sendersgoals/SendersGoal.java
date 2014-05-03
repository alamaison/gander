package uk.ac.ic.doc.gander.flowinference.sendersgoals;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.dda.Goal;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * Goal whose solution is the set of model sites that make a call matching the
 * goal's criteria.
 */
public interface SendersGoal extends Goal<Result<ModelSite<Call>>> {

}
