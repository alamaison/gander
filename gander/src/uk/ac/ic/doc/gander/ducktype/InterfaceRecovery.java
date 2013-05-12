package uk.ac.ic.doc.gander.ducktype;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.interfacetype.Feature;
import uk.ac.ic.doc.gander.model.OldNamespace;

/**
 * Program analysis to infer interface types for expressions.
 * 
 * Uses evidence of the interface from the way values are used to recover as
 * much of the interface as possible.
 * 
 * The analysis is sound as long as the program is well formed. It will only
 * include features that the values at the expression definitely support but may
 * not include all such features. Well formed is taken to mean that no
 * AttributeErrors occur at run time.
 */
public final class InterfaceRecovery {

	private final TypeResolver resolver;

	public InterfaceRecovery(TypeResolver resolver) {
		this.resolver = resolver;
	}

	public DuckType inferDuckType(exprType expression,
			BasicBlock containingBlock, OldNamespace scope,
			boolean excludeCurrentFeature) {

		Set<Call> dependentCalls = new CallTargetSignatureBuilder()
				.interfaceType(expression, containingBlock, scope, resolver,
						excludeCurrentFeature);

		Set<String> methods = SignatureHelper
				.convertSignatureToMethodNames(dependentCalls);

		Set<Feature> features = new HashSet<Feature>();
		for (String name : methods) {
			features.add(new NamedMethodFeature(name));
		}

		return new DuckType(features);
	}
}
