package uk.ac.ic.doc.gander.duckinference;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.analysis.inheritance.CachingInheritanceTree;
import uk.ac.ic.doc.gander.analysis.inheritance.InheritedMethods;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.ducktype.DuckType;
import uk.ac.ic.doc.gander.ducktype.InterfaceRecovery;
import uk.ac.ic.doc.gander.ducktype.NamedMethodFeature;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TObject;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.interfacetype.Feature;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.OldNamespace;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;

public class DuckTyper {
	private final LoadedTypeDefinitions definitions;
	private final TypeResolver resolver;
	private long duckTimeSheet = 0;

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
			OldNamespace scope, boolean excludeCurrentFeature) {

		long oldFlowCost = resolver.flowCost();

		long start = System.currentTimeMillis();

		InterfaceRecovery inferenceEngine = new InterfaceRecovery(resolver);
		DuckType recoveredInterface = inferenceEngine.inferDuckType(expression,
				containingBlock, scope, excludeCurrentFeature);

		Set<String> methods = new HashSet<String>();
		for (Feature feature : recoveredInterface)
		{
			if (feature instanceof NamedMethodFeature)
			{
				methods.add(((NamedMethodFeature) feature).name());
			}
		}

		Result<Type> result;
		if (methods.isEmpty()) {
			result = TopT.INSTANCE;

		} else {

			Set<Type> type = new HashSet<Type>();

			for (ClassCO klass : definitions.getDefinitions()) {
				InheritedMethods inheritance = new InheritedMethods(
						new CachingInheritanceTree(
								klass.oldStyleConflatedNamespace(), resolver));

				// TODO: We only compare by name. Matching parameter numbers etc
				// will require more complex logic.
				if (inheritance.methodsInTree().containsAll(methods)) {
					type.add(new TObject(klass));
					/*
					 * TODO: without look at the parameters, can't tell class
					 * and instance apart
					 */
					type.add(new TClass(klass));
				}
			}

			result = new FiniteResult<Type>(type);
		}

		long now = System.currentTimeMillis();
		duckTimeSheet += (now - start) - (resolver.flowCost() - oldFlowCost);

		return result;
	}

	public Result<Type> typeOf(exprType expression, BasicBlock containingBlock,
			OldNamespace scope) {
		return typeOf(expression, containingBlock, scope, false);
	}

	public long duckCost() {
		return duckTimeSheet;
	}
}
