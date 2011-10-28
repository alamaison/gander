package uk.ac.ic.doc.gander.model.sitefinders;

import java.util.Collections;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.sitefinders.CallSitesFinder.Predicate;

/**
 * Find all call sites.
 * 
 * Each call site consists of an AST node and its enclosing model namespace.
 */
public final class AllCallSitesFinder {

	private final Set<ModelSite<Call>> sites;

	public AllCallSitesFinder(Model model) {

		sites = new CallSitesFinder(model, new Predicate() {

			public boolean isMatch(ModelSite<Call> callSite) {
				return true;
			}
			
		}).getSites();
	}

	public Set<ModelSite<Call>> getSites() {
		return Collections.unmodifiableSet(sites);
	}
}
