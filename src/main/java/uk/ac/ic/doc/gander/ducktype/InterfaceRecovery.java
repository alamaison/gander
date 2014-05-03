package uk.ac.ic.doc.gander.ducktype;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.CallHelper;
import uk.ac.ic.doc.gander.Feature;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.interfacetype.InterfaceType;
import uk.ac.ic.doc.gander.interfacetype.InterfaceTypeSystem;
import uk.ac.ic.doc.gander.model.ModelSite;
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
public final class InterfaceRecovery implements InterfaceTypeSystem {

    private final TypeResolver resolver;
    private boolean excludeCurrentFeature;

    public InterfaceRecovery(TypeResolver resolver,
            boolean excludeCurrentFeature) {
        this.resolver = resolver;
        this.excludeCurrentFeature = excludeCurrentFeature;
    }

    public InterfaceType inferDuckType(exprType expression,
            BasicBlock containingBlock, OldNamespace scope) {

        Set<Call> dependentCalls = new CallTargetSignatureBuilder()
                .interfaceType(expression, containingBlock, scope, resolver,
                        excludeCurrentFeature);

        Set<Feature> features = new HashSet<Feature>();
        for (Call call : dependentCalls) {
            features.add(new NamedMethodFeature(CallHelper
                    .indirectCallName(call)));
        }

        return new DuckType(features);
    }

    @Override
    public InterfaceType typeOf(ModelSite<? extends exprType> expression,
            BasicBlock containingBlock) {

        return inferDuckType(expression.astNode(), containingBlock, expression
                .codeObject().oldStyleConflatedNamespace());
    }
}
