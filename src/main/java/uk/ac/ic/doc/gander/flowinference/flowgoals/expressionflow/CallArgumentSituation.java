package uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow;

import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.abstractmachine.FunctionStylePassingStrategy;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyCallable;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.CallsiteArgument;
import uk.ac.ic.doc.gander.flowinference.call.CallDispatch;
import uk.ac.ic.doc.gander.flowinference.callframe.CallSiteStackFrame;
import uk.ac.ic.doc.gander.flowinference.callframe.StackFrame;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopFp;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.typegoals.expression.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.model.ModelSite;

final class CallArgumentSituation implements FlowSituation {

    private final ModelSite<Call> callSite;
    private final CallsiteArgument argument;
    private final StackFrame<Argument> stackFrame;

    public CallArgumentSituation(ModelSite<Call> callSite,
            CallsiteArgument argument) {
        this.callSite = callSite;
        this.argument = argument;

        stackFrame = new CallSiteStackFrame(callSite);
    }

    @Override
    public Result<FlowPosition> nextFlowPositions(
            final SubgoalManager goalManager) {
        Result<PyObject> receivers = goalManager
                .registerSubgoal(new ExpressionTypeGoal(
                        new ModelSite<exprType>(callSite.astNode().func,
                                callSite.codeObject())));

        return receivers
                .transformResult(new Transformer<PyObject, Result<FlowPosition>>() {

                    @Override
                    public Result<FlowPosition> transformFiniteResult(
                            Set<PyObject> receiverTypes) {
                        return nextPositions(receiverTypes, goalManager);
                    }

                    @Override
                    public Result<FlowPosition> transformInfiniteResult() {
                        /*
                         * We've lost track of where the argument flows to so no
                         * choice but to surrender.
                         */
                        return TopFp.INSTANCE;
                    }
                });
    }

    private Result<FlowPosition> nextPositions(Set<PyObject> receiverTypes,
            SubgoalManager goalManager) {
        RedundancyEliminator<FlowPosition> nextPositions = new RedundancyEliminator<FlowPosition>();

        for (PyObject receiver : receiverTypes) {
            nextPositions.add(parametersOf(receiver, goalManager));
            if (nextPositions.isFinished())
                break;
        }

        return nextPositions.result();
    }

    /**
     * Finds the flow positions that the argument might flow when invoking a
     * particular receiver.
     */
    private Result<FlowPosition> parametersOf(PyObject receiver,
            SubgoalManager goalManager) {

        if (receiver instanceof PyCallable) {

            Result<CallDispatch> calls = ((PyCallable) receiver).dispatches(
                    stackFrame, goalManager);

            return calls.transformResult(new ArgumentFlower(argument(),
                    goalManager));

        } else {
            /*
             * XXX: just because the analysis thinks this might be happening,
             * doesn't mean that it will. Code might be correct in practice.
             */
            System.err
                    .println("UNTYPABLE: Can't call non-callable code object");
            return FiniteResult.bottom();
        }
    }

    private Argument argument() {
        return argument
                .mapToActualArgument(FunctionStylePassingStrategy.INSTANCE);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((argument == null) ? 0 : argument.hashCode());
        result = prime * result
                + ((callSite == null) ? 0 : callSite.hashCode());
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
        CallArgumentSituation other = (CallArgumentSituation) obj;
        if (argument == null) {
            if (other.argument != null)
                return false;
        } else if (!argument.equals(other.argument))
            return false;
        if (callSite == null) {
            if (other.callSite != null)
                return false;
        } else if (!callSite.equals(other.callSite))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CallArgumentSituation [argument=" + argument + ", callSite="
                + callSite + "]";
    }

}
