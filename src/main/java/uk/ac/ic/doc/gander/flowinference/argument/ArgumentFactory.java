package uk.ac.ic.doc.gander.flowinference.argument;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;
import org.python.pydev.parser.jython.ast.keywordType;

import uk.ac.ic.doc.gander.model.ModelSite;

public enum ArgumentFactory {

    INSTANCE;

    /**
     * Search call-site for an argument that matches the given expression.
     * 
     * @param callSite
     * @param argumentToMatch
     *            the AST expression being searched for
     * 
     * @return the argument representation or {@code null} if no such argument
     *         matched the given AST expression.
     */
    public CallsiteArgument searchCallSite(ModelSite<Call> callSite,
            final exprType argumentToMatch) {

        if (callSite == null)
            throw new NullPointerException("Call-site required");
        if (argumentToMatch == null)
            throw new NullPointerException(
                    "Must have argument to match against");

        final CallsiteArgument[] argument = { null };

        sniffArguments(callSite, new SniffEvents() {

            private boolean isMatch(ModelSite<exprType> candidate) {
                return candidate.astNode().equals(argumentToMatch);
            }

            @Override
            public boolean sniffedExpandedIterable(
                    ModelSite<exprType> iterableArgument) {

                if (isMatch(iterableArgument)) {
                    argument[0] = argForExpandedIterable(iterableArgument);
                }

                return argument[0] != null;
            }

            @Override
            public boolean sniffedPositionalArgument(int position,
                    ModelSite<exprType> positonalArgument) {

                if (isMatch(positonalArgument)) {
                    argument[0] = argForPosition(position, positonalArgument);
                }

                return argument[0] != null;
            }

            @Override
            public boolean sniffedExpandedMap(ModelSite<exprType> mapArgument) {

                if (isMatch(mapArgument)) {
                    argument[0] = argForExpandedMap(mapArgument);
                }

                return argument[0] != null;
            }

            @Override
            public boolean sniffedKeywordArgument(
                    ModelSite<keywordType> keywordArgument) {

                ModelSite<exprType> value = new ModelSite<exprType>(
                        keywordArgument.astNode().value, keywordArgument
                                .codeObject());
                if (isMatch(value)) {
                    argument[0] = new ExplicitKeywordCallsiteArgument(
                            keywordArgument);
                }

                return argument[0] != null;
            }
        });

        return argument[0];
    }

    public Set<CallsiteArgument> fromCallSite(ModelSite<Call> callSite) {
        if (callSite == null)
            throw new NullPointerException("Call-site required");

        final Set<CallsiteArgument> arguments = new HashSet<CallsiteArgument>();

        sniffArguments(callSite, new SniffEvents() {

            @Override
            public boolean sniffedExpandedIterable(ModelSite<exprType> argument) {
                arguments.add(argForExpandedIterable(argument));
                return false;
            }

            @Override
            public boolean sniffedPositionalArgument(int position,
                    ModelSite<exprType> argument) {
                arguments.add(argForPosition(position, argument));
                return false;
            }

            @Override
            public boolean sniffedExpandedMap(ModelSite<exprType> argument) {
                arguments.add(argForExpandedMap(argument));
                return false;
            }

            @Override
            public boolean sniffedKeywordArgument(
                    ModelSite<keywordType> argument) {
                arguments.add(argForKeyword(argument));
                return false;
            }
        });

        return arguments;
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

    private interface SniffEvents {

        boolean sniffedExpandedIterable(ModelSite<exprType> argument);

        boolean sniffedPositionalArgument(int position,
                ModelSite<exprType> argument);

        boolean sniffedExpandedMap(ModelSite<exprType> argument);

        boolean sniffedKeywordArgument(ModelSite<keywordType> argument);

    }

    private void sniffArguments(ModelSite<Call> callSite,
            SniffEvents eventHandler) {

        Call node = callSite.astNode();

        if (node.starargs != null) {
            ModelSite<exprType> argument = new ModelSite<exprType>(
                    node.starargs, callSite.codeObject());

            if (eventHandler.sniffedExpandedIterable(argument))
                return;
        }

        if (node.kwargs != null) {
            ModelSite<exprType> argument = new ModelSite<exprType>(node.kwargs,
                    callSite.codeObject());

            if (eventHandler.sniffedExpandedMap(argument))
                return;
        }

        for (int i = 0; i < node.args.length; ++i) {
            ModelSite<exprType> argument = new ModelSite<exprType>(
                    node.args[i], callSite.codeObject());

            if (eventHandler.sniffedPositionalArgument(i, argument))
                return;
        }

        for (int i = 0; i < node.keywords.length; ++i) {
            ModelSite<keywordType> argument = new ModelSite<keywordType>(
                    node.keywords[i], callSite.codeObject());

            if (eventHandler.sniffedKeywordArgument(argument))
                return;
        }
    }
}
