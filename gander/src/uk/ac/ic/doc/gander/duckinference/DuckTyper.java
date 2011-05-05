package uk.ac.ic.doc.gander.duckinference;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.analysis.inheritance.CachingInheritanceTree;
import uk.ac.ic.doc.gander.analysis.inheritance.InheritedMethods;
import uk.ac.ic.doc.gander.analysis.signatures.CallTargetSignatureBuilder;
import uk.ac.ic.doc.gander.analysis.signatures.SignatureHelper;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Namespace;

public class DuckTyper {
	private final LoadedTypeDefinitions definitions;
	private TypeResolver resolver;

	public DuckTyper(Model model, TypeResolver resolver) {
		this.resolver = resolver;
		definitions = new LoadedTypeDefinitions(model);
	}

	/**
	 * Render a type judgement for a method call's target.
	 * 
	 * TODO: Really the {@link DuckTyper} should only do the hierarchy search
	 * part. The dependents method name lookup should be done elsewhere so that
	 * it's not limited to call targets and can be used on variables in general.
	 * 
	 * @param call
	 *            The call whose target we want to infer a type for.
	 * @param containingBlock
	 *            The basic block containing the call in question.
	 * @param scope
	 *            The Python scope in which the call occurs.
	 * @return A type judgement as a set of {@link Type}s.
	 */
	public Set<Type> typeOf(Call call, BasicBlock containingBlock,
			Namespace scope) {

		Set<String> methods = calculateDependentMethodNames(call,
				containingBlock, scope);

		Set<Type> type = new HashSet<Type>();

		for (Class klass : definitions.getDefinitions()) {
			InheritedMethods inheritance = new InheritedMethods(
					new CachingInheritanceTree(klass, resolver));

			// TODO: We only compare by name. Matching parameter numbers etc
			// will require more complex logic.
			if (inheritance.methodsInTree().containsAll(methods))
				type.add(new TClass(klass));
		}

		return type;
	}

	private Set<String> calculateDependentMethodNames(Call call,
			BasicBlock containingBlock, Namespace scope) {

		Set<Call> dependentCalls = new CallTargetSignatureBuilder()
				.signatureOfTarget(call, containingBlock, scope, resolver);

		return SignatureHelper.convertSignatureToMethodNames(dependentCalls);
	}
}
