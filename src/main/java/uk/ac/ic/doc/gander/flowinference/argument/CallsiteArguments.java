package uk.ac.ic.doc.gander.flowinference.argument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;
import org.python.pydev.parser.jython.ast.keywordType;

import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * All the various arguments at a given syntactic call-site.
 */
public final class CallsiteArguments {

    private final List<CallsiteArgument> positionals = new ArrayList<CallsiteArgument>();
    private final Map<String, CallsiteArgument> keywords = new HashMap<String, CallsiteArgument>();
    private final CallsiteArgument expandedIterable;
    private final CallsiteArgument expandedMapping;

    public CallsiteArguments(ModelSite<Call> astCallSite) {

        Call node = astCallSite.astNode();

        for (int i = 0; i < node.args.length; ++i) {
            ModelSite<exprType> argument = new ModelSite<exprType>(
                    node.args[i], astCallSite.codeObject());

            ExplicitPositionalCallsiteArgument positional = argForPosition(i,
                    argument);
            assert positional.position() == positionals.size();

            positionals.add(positional);
        }

        assert positionals.size() == node.args.length;

        for (int i = 0; i < node.keywords.length; ++i) {
            ModelSite<keywordType> argument = new ModelSite<keywordType>(
                    node.keywords[i], astCallSite.codeObject());

            ExplicitKeywordCallsiteArgument kwarg = argForKeyword(argument);
            keywords.put(kwarg.keyword(), kwarg);
        }

        assert keywords.size() == node.keywords.length;

        if (node.starargs != null) {
            ModelSite<exprType> argument = new ModelSite<exprType>(
                    node.starargs, astCallSite.codeObject());

            expandedIterable = argForExpandedIterable(argument);
        } else {
            expandedIterable = null;
        }

        if (node.kwargs != null) {
            ModelSite<exprType> argument = new ModelSite<exprType>(node.kwargs,
                    astCallSite.codeObject());

            expandedMapping = argForExpandedMap(argument);
        } else {
            expandedMapping = null;
        }
    }

    List<CallsiteArgument> positionals() {
        return Collections.unmodifiableList(positionals);
    }

    Map<String, CallsiteArgument> keywords() {
        return Collections.unmodifiableMap(keywords);
    }

    CallsiteArgument expandedIterable() {
        return expandedIterable;
    }

    CallsiteArgument expandedMapping() {
        return expandedMapping;
    }

    private ExpandedIterableCallsiteArgument argForExpandedIterable(
            ModelSite<exprType> argument) {
        return new ExpandedIterableCallsiteArgument(argument);
    }

    private ExplicitKeywordCallsiteArgument argForKeyword(
            ModelSite<keywordType> argument) {
        return new ExplicitKeywordCallsiteArgument(argument);
    }

    private ExpandedMapCallsiteArgument argForExpandedMap(
            ModelSite<exprType> argument) {
        return new ExpandedMapCallsiteArgument(argument);
    }

    private ExplicitPositionalCallsiteArgument argForPosition(int position,
            ModelSite<exprType> argument) {
        return new ExplicitPositionalCallsiteArgument(argument, position);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((expandedIterable == null) ? 0 : expandedIterable.hashCode());
        result = prime * result
                + ((expandedMapping == null) ? 0 : expandedMapping.hashCode());
        result = prime * result
                + ((keywords == null) ? 0 : keywords.hashCode());
        result = prime * result
                + ((positionals == null) ? 0 : positionals.hashCode());
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
        CallsiteArguments other = (CallsiteArguments) obj;
        if (expandedIterable == null) {
            if (other.expandedIterable != null)
                return false;
        } else if (!expandedIterable.equals(other.expandedIterable))
            return false;
        if (expandedMapping == null) {
            if (other.expandedMapping != null)
                return false;
        } else if (!expandedMapping.equals(other.expandedMapping))
            return false;
        if (keywords == null) {
            if (other.keywords != null)
                return false;
        } else if (!keywords.equals(other.keywords))
            return false;
        if (positionals == null) {
            if (other.positionals != null)
                return false;
        } else if (!positionals.equals(other.positionals))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CallsiteArguments [positionals=" + positionals + ", keywords="
                + keywords + ", expandedIterable=" + expandedIterable
                + ", expandedMapping=" + expandedMapping + "]";
    }

}
