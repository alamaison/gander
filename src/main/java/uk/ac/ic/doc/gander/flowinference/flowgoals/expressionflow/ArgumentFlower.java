package uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow;

import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.call.CallDispatch;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopFp;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;

public final class ArgumentFlower implements
        Transformer<CallDispatch, Result<FlowPosition>> {

    private final SubgoalManager goalManager;
    private final Argument argument;

    public ArgumentFlower(Argument argument, SubgoalManager goalManager) {
        this.argument = argument;
        this.goalManager = goalManager;
    }

    @Override
    public Result<FlowPosition> transformFiniteResult(Set<CallDispatch> calls) {

        RedundancyEliminator<ArgumentDestination> destinations = new RedundancyEliminator<ArgumentDestination>();

        for (CallDispatch call : calls) {

            destinations.add(call.destinationsReceivingArgument(argument,
                    goalManager));
            if (destinations.isFinished())
                break;
        }

        return destinations.result().transformResult(
                new ReceivingParameterPositioner());
    }

    @Override
    public Result<FlowPosition> transformInfiniteResult() {
        return TopFp.INSTANCE;
    }
}