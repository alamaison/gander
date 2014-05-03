package uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow;

import java.util.Set;

import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyCallable;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.argument.Argument;
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

final class AttributeCallReceiverSituation implements FlowSituation {

    private final ModelSite<Attribute> receiver;
    private final ModelSite<Call> callSite;

    AttributeCallReceiverSituation(ModelSite<Call> callSite,
            ModelSite<Attribute> receiver) {
        this.callSite = callSite;
        this.receiver = receiver;
    }

    @Override
    public Result<FlowPosition> nextFlowPositions(
            final SubgoalManager goalManager) {

        Result<PyObject> receivers = goalManager
                .registerSubgoal(new ExpressionTypeGoal(receiver));

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
            nextPositions.add(selfParameterOf(receiver, goalManager));
            if (nextPositions.isFinished()) {
                break;
            }
        }

        return nextPositions.result();
    }

    /**
     * Finds the flow position that the attribute LHS might flow when the
     * attribute is the receiver of a call.
     */
    private Result<FlowPosition> selfParameterOf(PyObject receiver,
            SubgoalManager goalManager) {

        if (receiver instanceof PyCallable) {

            PyCallable callable = (PyCallable) receiver;

            StackFrame<Argument> stackFrame = new CallSiteStackFrame(callSite);

            Result<CallDispatch> calls = callable.dispatches(stackFrame,
                    goalManager);

            return calls.transformResult(new ArgumentFlower(
                    callable.selfArgument(), goalManager));
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((receiver == null) ? 0 : receiver.hashCode());
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
        AttributeCallReceiverSituation other = (AttributeCallReceiverSituation) obj;
        if (receiver == null) {
            if (other.receiver != null)
                return false;
        } else if (!receiver.equals(other.receiver))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AttributeCallReceiverSituation [receiver=" + receiver + "]";
    }

}
