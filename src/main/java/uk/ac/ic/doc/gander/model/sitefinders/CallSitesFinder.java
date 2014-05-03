package uk.ac.ic.doc.gander.model.sitefinders;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.model.CallSitesWalker;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.CallSitesWalker.EventHandler;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

/**
 * Find all call sites in the loaded model matching a predicate.
 * 
 * Each call site consists of an AST node and its enclosing model namespace.
 */
public final class CallSitesFinder {

    public interface Predicate {
        boolean isMatch(ModelSite<Call> callSite);
    }

    private final Set<ModelSite<Call>> sites = new HashSet<ModelSite<Call>>();

    public CallSitesFinder(final Model model, final Predicate predicate) {

        new CallSitesWalker(model, new EventHandler() {

            public void encounteredCallSite(Call call, CodeObject codeObject) {

                ModelSite<Call> callSite = new ModelSite<Call>(call, codeObject);
                
                if (predicate.isMatch(callSite)) {
                    sites.add(callSite);
                }
            }

        });

    }

    public Set<ModelSite<Call>> getSites() {
        return Collections.unmodifiableSet(sites);
    }
}
