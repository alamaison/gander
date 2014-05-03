package uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopFp;
import uk.ac.ic.doc.gander.flowinference.result.Result;

final class EscapeSituation implements FlowSituation {

    public static final EscapeSituation INSTANCE = new EscapeSituation();

    private EscapeSituation() {
    }

    @Override
    public Result<FlowPosition> nextFlowPositions(SubgoalManager goalManager) {
        return TopFp.INSTANCE;
    }

}
