package uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.Namespace;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.NamespaceNamePosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopFp;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.typegoals.expression.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.NamespaceName;

final class AttributeSituation implements FlowSituation {

    private final ModelSite<Attribute> attribute;

    /**
     * This situation keeps a reference to the node as its two textual values
     * (left and right of the dot) indicate the destination of the flow.
     */
    AttributeSituation(ModelSite<Attribute> attribute) {
        this.attribute = attribute;
    }

    /**
     * In a single step of execution Attribute instances flow into the matching
     * name of the attribute-accessible namespace of the types of object
     * possible at the LHS of the attribute.
     */
    @Override
    public Result<FlowPosition> nextFlowPositions(SubgoalManager goalManager) {

        Result<PyObject> lhs = goalManager.registerSubgoal(new ExpressionTypeGoal(
                new ModelSite<exprType>(attribute.astNode().value, attribute
                        .codeObject())));

        return lhs
                .transformResult(new Transformer<PyObject, Result<FlowPosition>>() {

                    @Override
                    public Result<FlowPosition> transformFiniteResult(
                            Set<PyObject> lhsObjects) {
                        Set<FlowPosition> positions = new HashSet<FlowPosition>();

                        for (PyObject object : lhsObjects) {
                            /*
                             * TODO: can we take advantage of the fact that
                             * generally this will be limited to the writeable
                             * namespace because it only flows by being written?
                             * Or is there some way that a read-only namespace
                             * can flow here?
                             */
                            for (Namespace namespace : object
                                    .memberReadableNamespaces()) {
                                NamespaceName name = new NamespaceName(
                                        ((NameTok) attribute.astNode().attr).id,
                                        namespace);
                                positions.add(new NamespaceNamePosition(name));
                            }
                        }

                        return new FiniteResult<FlowPosition>(positions);
                    }

                    @Override
                    public Result<FlowPosition> transformInfiniteResult() {
                        return TopFp.INSTANCE;
                    }
                });
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((attribute == null) ? 0 : attribute.hashCode());
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
        AttributeSituation other = (AttributeSituation) obj;
        if (attribute == null) {
            if (other.attribute != null)
                return false;
        } else if (!attribute.equals(other.attribute))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AttributeSituation [attribute=" + attribute + "]";
    }

}
