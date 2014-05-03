package uk.ac.ic.doc.gander.model.codeblock;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.ic.doc.gander.model.name_binding.LocallyBoundNameFinder;
import uk.ac.ic.doc.gander.model.name_binding.Variable;
import uk.ac.ic.doc.gander.model.parameters.FormalParameter;

/**
 * For a given code block, find the set of names that bound in it.
 * 
 * This is not the same thing as the local variables of the block as they may be
 * the subject of a 'global' declaration in that block. It also doesn't include
 * bindings that occur in declarations such as nested functions and classes that
 * create a new code block.
 */
final class CodeBlockBoundVariablesFinder {

    static Set<String> boundVariables(DefaultCodeBlock codeBlock) {
        assert codeBlock != null;

        final Set<String> boundNames = new HashSet<String>();

        List<FormalParameter> bindingParameters = codeBlock.formalParameters().parameters();
        for (FormalParameter parameter : bindingParameters) {
            for (Variable variable : parameter.boundVariables()) {
                boundNames.add(variable.name());
            }
        }

        try {
            codeBlock.accept(new LocallyBoundNameFinder() {

                @Override
                protected void onNameBound(String name) {
                    boundNames.add(name);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return boundNames;
    }

}
