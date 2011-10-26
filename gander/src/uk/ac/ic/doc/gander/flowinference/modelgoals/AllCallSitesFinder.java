package uk.ac.ic.doc.gander.flowinference.modelgoals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.model.CallSitesWalker;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.CallSitesWalker.EventHandler;

/**
 * Find all call sites.
 * 
 * Each call site consists of an AST node and its enclosing model namespace.
 */
final class AllCallSitesFinder {

	private final Set<ModelSite<Call>> sites = new HashSet<ModelSite<Call>>();

	AllCallSitesFinder(Model model) {
		
		new CallSitesWalker(model, new EventHandler() {
			public void encounteredCallSite(Call call, Namespace namespace) {
				sites.add(new ModelSite<Call>(call, namespace));
			}
		});
		
	}

	Set<ModelSite<Call>> getSites() {
		return Collections.unmodifiableSet(sites);
	}
}
