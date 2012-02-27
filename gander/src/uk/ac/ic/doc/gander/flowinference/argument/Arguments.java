package uk.ac.ic.doc.gander.flowinference.argument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.ic.doc.gander.flowinference.callframe.ArgumentPassingStrategy;

/**
 * The arguments that are really present at a call-site once the interpreter has
 * done it's magic.
 * 
 * This is a desugared version of the syntactic call-site.
 */
public final class Arguments {

	private final List<Argument> positionals = new ArrayList<Argument>();
	private final Map<String, Argument> keywords = new HashMap<String, Argument>();
	private final Argument expandedIterable;
	private final Argument expandedMapping;

	public Arguments(CallsiteArguments callSite,
			ArgumentPassingStrategy passingStrategy) {

		if (passingStrategy.passesHiddenSelf()) {
			positionals.add(passingStrategy.selfArgument());
		}

		for (CallsiteArgument callsiteArg : callSite.positionals()) {

			Argument argument = callsiteArg
					.mapToActualArgument(passingStrategy);

			positionals.add(argument);
		}

		Map<String, CallsiteArgument> callsiteKeywords = callSite.keywords();
		for (Entry<String, CallsiteArgument> callsiteArg : callsiteKeywords
				.entrySet()) {

			Argument argument = callsiteArg.getValue().mapToActualArgument(
					passingStrategy);
			keywords.put(callsiteArg.getKey(), argument);
		}

		CallsiteArgument callSiteArg = callSite.expandedIterable();
		if (callSiteArg != null) {
			expandedIterable = callSiteArg.mapToActualArgument(passingStrategy);
		} else {
			expandedIterable = null;
		}

		callSiteArg = callSite.expandedMapping();
		if (callSiteArg != null) {
			expandedMapping = callSiteArg.mapToActualArgument(passingStrategy);
		} else {
			expandedMapping = null;
		}
	}

	public List<Argument> positionals() {
		return Collections.unmodifiableList(positionals);
	}

	public Map<String, Argument> keywords() {
		return Collections.unmodifiableMap(keywords);
	}

	public Argument expandedIterable() {
		return expandedIterable;
	}

	public Argument expandedMapping() {
		return expandedMapping;
	}
}
