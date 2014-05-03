package uk.ac.ic.doc.gander.flowinference.flowgoals;

import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

/**
 * Model of a code object's flow in a single execution step.
 * 
 * Code objects such as modules, classes and functions are Python objects like
 * any other. Their 'value' can flow into any named reference in their enclosing
 * code block as well as anywhere they are explicitly imported.
 * 
 * Code objects are flowed using this special CodeObjectDefinitionPosition
 * because they don't have expressions to represent them. They have to be
 * modelled specially.
 */
public final class CodeObjectDefinitionPosition implements FlowPosition {

    private final CodeObject codeObject;

    public CodeObjectDefinitionPosition(CodeObject codeObject) {
        this.codeObject = codeObject;
    }

    public FlowStepGoal nextStepGoal() {
        return new CodeObjectDefinitionFlowStepGoal(codeObject);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((codeObject == null) ? 0 : codeObject.hashCode());
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
        CodeObjectDefinitionPosition other = (CodeObjectDefinitionPosition) obj;
        if (codeObject == null) {
            if (other.codeObject != null)
                return false;
        } else if (!codeObject.equals(other.codeObject))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CodeObjectDefinitionPosition [codeObject=" + codeObject + "]";
    }

}
