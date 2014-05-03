package uk.ac.ic.doc.gander.model.sitefinders;

import java.util.Collections;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.sitefinders.CallSitesFinder.Predicate;

/**
 * Find all call sites with the given number of arguments.
 * 
 * Each call site consists of an AST node and its enclosing model namespace.
 */
public final class CardinalCallSitesFinder {

    private final Set<ModelSite<Call>> sites;

    public CardinalCallSitesFinder(Model model, final int cardinality) {

        sites = new CallSitesFinder(model, new Predicate() {

            public boolean isMatch(ModelSite<Call> callSite) {
                return callSite.astNode().args.length == cardinality;
            }

        }).getSites();
    }

    public Set<ModelSite<Call>> getSites() {
        return Collections.unmodifiableSet(sites);
    }
}
