package uk.ac.ic.doc.gander.flowinference.argument;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.callframe.ArgumentPassingStrategy;
import uk.ac.ic.doc.gander.model.ModelSite;

final class ExpandedMapCallsiteArgument implements CallsiteArgument {

    private final ModelSite<exprType> argument;

    ExpandedMapCallsiteArgument(ModelSite<exprType> argument) {
        assert argument != null;
        this.argument = argument;
    }

    @Override
    public Argument mapToActualArgument(ArgumentPassingStrategy argumentMapper) {
        return new ExpandedMapArgument(argument);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((argument == null) ? 0 : argument.hashCode());
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
        ExpandedMapCallsiteArgument other = (ExpandedMapCallsiteArgument) obj;
        if (argument == null) {
            if (other.argument != null)
                return false;
        } else if (!argument.equals(other.argument))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ExpandedMapCallsiteArgument [argument=" + argument + "]";
    }

}
