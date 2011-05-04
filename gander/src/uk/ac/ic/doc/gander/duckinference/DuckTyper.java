package uk.ac.ic.doc.gander.duckinference;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.gander.CallHelper;
import uk.ac.ic.doc.gander.analysis.inheritance.CachingInheritanceTree;
import uk.ac.ic.doc.gander.analysis.inheritance.InheritedMethods;
import uk.ac.ic.doc.gander.analysis.signatures.SignatureBuilder;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Namespace;

public class DuckTyper {
	private final LoadedTypeDefinitions definitions;
	private TypeResolver resolver;

	public DuckTyper(Model model, TypeResolver resolver) {
		this.resolver = resolver;
		definitions = new LoadedTypeDefinitions(model);
	}

	public Set<Type> typeOf(Call call, BasicBlock containingBlock,
			Namespace scope) {

		Set<String> methods = calculateDependentMethodNames(call,
				containingBlock, scope);

		Set<Type> type = new HashSet<Type>();

		for (Class klass : definitions.getDefinitions()) {
			InheritedMethods inheritance = new InheritedMethods(
					new CachingInheritanceTree(klass, resolver));

			// XXX: We only compare by name. Matching parameter numbers etc
			// will require more complex logic.
			if (inheritance.methodsInTree().containsAll(methods))
				type.add(new TClass(klass));
		}

		return type;
	}

	private Set<String> calculateDependentMethodNames(Call call,
			BasicBlock containingBlock, Namespace scope) {

		Set<Call> dependentCalls;

		// if the call target isn't a simple variable name, we can still use
		// the name of the method being called as a single-item signature
		// TODO: This should be handled by the signature builder which shouldn't
		// insist the the call target be a Name. It should accept any expression
		// and return as much of the signature as it can calculate.
		if (!(CallHelper.indirectCallTarget(call) instanceof Name)) {
			dependentCalls = new HashSet<Call>();
			dependentCalls.add(call);
		} else {
			SignatureBuilder chainAnalyser = new SignatureBuilder();
			dependentCalls = chainAnalyser.signature((Name) CallHelper
					.indirectCallTarget(call), containingBlock,
					(Function) scope, resolver);
		}

		return convertCallsToMethodNames(dependentCalls);
	}

	private Set<String> convertCallsToMethodNames(Set<Call> dependentCalls) {
		Set<String> methods = new HashSet<String>();

		for (Call c : dependentCalls)
			methods.add(CallHelper.indirectCallName(c));

		return methods;
	}
}
