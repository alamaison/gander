package uk.ac.ic.doc.gander.duckinference;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.analysis.inheritance.CachingInheritanceTree;
import uk.ac.ic.doc.gander.analysis.inheritance.InheritedMethods;
import uk.ac.ic.doc.gander.analysis.signatures.CallTargetSignatureBuilder;
import uk.ac.ic.doc.gander.analysis.signatures.SignatureHelper;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.flowinference.types.TObject;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.OldNamespace;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;

public class DuckTyper {
	private final LoadedTypeDefinitions definitions;
	private final TypeResolver resolver;

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
	public Result<Type> typeOf(exprType expression, BasicBlock containingBlock,
			OldNamespace scope) {

		Set<Call> dependentCalls = new CallTargetSignatureBuilder()
				.interfaceType(expression, containingBlock, scope, resolver);

		Set<String> methods = SignatureHelper
				.convertSignatureToMethodNames(dependentCalls);

		if (methods.isEmpty()) {
			return TopT.INSTANCE;

		} else {

			Set<Type> type = new HashSet<Type>();

			for (ClassCO klass : definitions.getDefinitions()) {
				InheritedMethods inheritance = new InheritedMethods(
						new CachingInheritanceTree(
								klass.oldStyleConflatedNamespace(), resolver));

				// TODO: We only compare by name. Matching parameter numbers etc
				// will require more complex logic.
				if (inheritance.methodsInTree().containsAll(methods))
					type.add(new TObject(klass));
			}

			return new FiniteResult<Type>(type);
		}
	}
}
