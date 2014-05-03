package uk.ac.ic.doc.gander.flowinference.callframe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;

public final class StrategyBasedStackFrame implements StackFrame<Argument> {

    private final StackFrame<Argument> callFrame;
    private final ArgumentPassingStrategy passingStrategy;

    public StrategyBasedStackFrame(StackFrame<Argument> callFrame,
            ArgumentPassingStrategy passingStrategy) {

        this.callFrame = callFrame;
        this.passingStrategy = passingStrategy;
    }

    @Override
    public List<Argument> knownPositions() {

        if (passingStrategy.passesHiddenSelf()) {

            List<Argument> adjustedPositionals = new ArrayList<Argument>();
            adjustedPositionals.add(passingStrategy.selfArgument());

            for (Argument argument : callFrame.knownPositions()) {
                adjustedPositionals.add(argument);
            }

            return adjustedPositionals;

        } else {
            return callFrame.knownPositions();
        }
    }

    @Override
    public Map<String, Argument> knownKeywords() {
        return callFrame.knownKeywords();
    }

    @Override
    public boolean includesUnknownPositions() {
        return callFrame.includesUnknownPositions();
    }

    @Override
    public boolean includesUnknownKeywords() {
        return callFrame.includesUnknownKeywords();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((callFrame == null) ? 0 : callFrame.hashCode());
        result = prime * result
                + ((passingStrategy == null) ? 0 : passingStrategy.hashCode());
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
        StrategyBasedStackFrame other = (StrategyBasedStackFrame) obj;
        if (callFrame == null) {
            if (other.callFrame != null)
                return false;
        } else if (!callFrame.equals(other.callFrame))
            return false;
        if (passingStrategy == null) {
            if (other.passingStrategy != null)
                return false;
        } else if (!passingStrategy.equals(other.passingStrategy))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "StrategyBasedStackFrame [callFrame=" + callFrame
                + ", passingStrategy=" + passingStrategy + "]";
    }

}