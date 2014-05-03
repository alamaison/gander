package uk.ac.ic.doc.gander.model.codeblock;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.gander.model.name_binding.LocallyDeclaredGlobalFinder;

/**
 * For a given code block, find the set of names that are declared global.
 * 
 * This is not the same thing as the names that bind globally. Some names may be
 * free in this code block but bind globally because the only definition in
 * scope occurs in the global code block.
 */
final class CodeBlockGlobalsFinder {

    static Set<String> globals(CodeBlock codeBlock) {
        assert codeBlock != null;

        final Set<String> globals = new HashSet<String>();
        try {
            codeBlock.accept(new LocallyDeclaredGlobalFinder() {

                @Override
                protected boolean onGlobalDeclared(String name) {
                    globals.add(name);
                    return false;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return globals;
    }

}
