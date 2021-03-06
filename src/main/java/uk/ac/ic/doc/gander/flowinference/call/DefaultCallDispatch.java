package uk.ac.ic.doc.gander.flowinference.call;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.callframe.StackFrame;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Variable;
import uk.ac.ic.doc.gander.model.parameters.FormalParameter;
import uk.ac.ic.doc.gander.model.parameters.FormalParameters;

public final class DefaultCallDispatch implements CallDispatch {

    private final InvokableCodeObject receiver;
    private final StackFrame<Argument> stackFrame;

    public DefaultCallDispatch(InvokableCodeObject receiver,
            StackFrame<Argument> functionCall) {
        if (receiver == null)
            throw new NullPointerException(
                    "A representation of a call requires "
                            + "a code object to dispatch the call to");
        if (functionCall == null)
            throw new NullPointerException(
                    "A representation of a call requires a stack frame "
                            + "with the parameters being passed");

        this.receiver = receiver;
        this.stackFrame = functionCall;
    }

    @Override
    public Result<ArgumentDestination> destinationsReceivingArgument(
            Argument argument, SubgoalManager goalManager) {

        FormalParameters parameters = receiver.formalParameters();
        Set<Passage> passages = parameters.digestStackFrame(stackFrame);

        Set<ArgumentDestination> destinations = new HashSet<ArgumentDestination>();

        for (Passage passage : passages) {
            destinations.addAll(passage.destinationsOf(argument));
        }

        return new FiniteResult<ArgumentDestination>(destinations);
    }

    @Override
    public Result<PyObject> objectsBoundToVariable(Variable variable,
            SubgoalManager goalManager) {
        if (variable == null)
            throw new NullPointerException("Variable required");

        // Set<ParameterSource> sources = whatDoYouBindToVariable(variable);

        FormalParameter parameter = receiver.formalParameters()
                .variableBindingParameter(variable);

        return parameter.objectsPassedAtCall(stackFrame, variable, goalManager);
    }

    @Override
    public InvokableCodeObject receiver() {
        return receiver;
    }

    /*-
     private StackFrame<Argument> callFrame() {

     return sender.callFrame(callSite);
     }

     private Set<ParameterSource> whatDoYouBindToVariable(Variable variable) {
     if (variable == null)
     throw new NullPointerException("Variable required");

     FormalParameters parameters = receiver.formalParameters();

     if (parameters.hasVariableBindingParameter(variable)) {

     FormalParameter p = parameters.variableBindingParameter(variable);

     StackFrame callFrameForParameter = whatCallFrameDoesParameterConsume(
     parameters, p);

     return whatDoesParameterBindToVariable(p, variable,
     callFrameForParameter);

     } else {
     return Collections.emptySet();
     }
     }

     private StackFrame whatCallFrameDoesParameterConsume(
     FormalParameters parameters, FormalParameter p) {
     Map<FormalParameter, StackFrame> frames = parameters
     .digestCallFrame(callFrame());
     return frames.get(p);
     }

     private Set<ParameterSource> whatDoesParameterBindToVariable(
     FormalParameter p, Variable variable, StackFrame callFrame) {
     p.consumeCallFrame();
     }
     */

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((receiver == null) ? 0 : receiver.hashCode());
        result = prime * result
                + ((stackFrame == null) ? 0 : stackFrame.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultCallDispatch other = (DefaultCallDispatch) obj;
        if (receiver == null) {
            if (other.receiver != null)
                return false;
        } else if (!receiver.equals(other.receiver))
            return false;
        if (stackFrame == null) {
            if (other.stackFrame != null)
                return false;
        } else if (!stackFrame.equals(other.stackFrame))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DefaultCallDispatch [receiver=" + receiver + ", stackFrame="
                + stackFrame + "]";
    }

}
