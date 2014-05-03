package uk.ac.ic.doc.gander.model.parameters;

import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;

enum DefaultStarargsArgument implements Argument {

    INSTANCE;

    @Override
    public ArgumentDestination passArgumentAtCall(InvokableCodeObject receiver) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<PyObject> type(SubgoalManager goalManager) {
        // TODO: return builtin tuple
        return TopT.INSTANCE;
    }

}
