package uk.ac.ic.doc.gander.duckinference;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.MethodCallHelper;
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
		SignatureBuilder chainAnalyser = new SignatureBuilder();
		Set<Call> dependentCalls = chainAnalyser.signature(MethodCallHelper
				.extractMethodCallTarget(call), containingBlock,
				(Function) scope, resolver);

		return convertCallsToMethodNames(dependentCalls);
	}

	private Set<String> convertCallsToMethodNames(Set<Call> dependentCalls) {
		Set<String> methods = new HashSet<String>();

		for (Call c : dependentCalls)
			methods.add(MethodCallHelper.extractMethodCallName(c).id);

		return methods;
	}
}
