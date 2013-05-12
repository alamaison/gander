package uk.ac.ic.doc.gander.analysis.signatures;

import java.util.Collections;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.VisitorBase;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.CallHelper;
import uk.ac.ic.doc.gander.ast.AstParentNodeFinder;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.model.OldNamespace;

/**
 * Creates type signatures specifically for the target of an indirect call.
 * 
 * In other words, for a statement of the form {@code x.a.f()} this tries to
 * build a type signature for {@code x.a}.
 * 
 * In practice, good quality signatures (those include more than one method
 * name) are only generated for calls directly on a variable, {@code x.f()}.
 * Other expression such as {@code c().f()} or {@code x.y.f()} will only include
 * the name of the callable, {@code f} in the signature.
 * 
 * Using this class to generate signatures for direct calls is an error.
 */
public class CallTargetSignatureBuilder {

	public Set<Call> signatureOfTarget(Call call, BasicBlock containingBlock,
			OldNamespace scope, TypeResolver resolver,
			boolean excludeCurrentFeature) {
		if (!CallHelper.isIndirectCall(call)) {

			return Collections.emptySet();
		} else {

			return interfaceType(CallHelper.indirectCallTarget(call),
					containingBlock, scope, resolver, excludeCurrentFeature);
		}
	}
	public Set<Call> signatureOfTarget(Call call, BasicBlock containingBlock,
			OldNamespace scope, TypeResolver resolver) {
		return signatureOfTarget(call, containingBlock, scope, resolver, false);
	}

	public Set<Call> interfaceType(exprType expression,
			BasicBlock containingBlock, OldNamespace scope,
			TypeResolver resolver, boolean excludeCurrentFeature) {

		try {
			return (Set<Call>) expression.accept(new SignatureMapper(
					containingBlock, scope, resolver, true, true,
					excludeCurrentFeature));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private class SignatureMapper extends VisitorBase {

		private final OldNamespace scope;
		private final BasicBlock containingBlock;
		private final TypeResolver resolver;
		private final boolean includeRequiredFeatures;
		private final boolean includeFstr;
		private final boolean excludeCurrentFeature;

		SignatureMapper(BasicBlock containingBlock, OldNamespace scope,
				TypeResolver resolver, boolean includeRequiredFeatures,
				boolean includeFstr, boolean excludeCurrentFeature) {
			this.containingBlock = containingBlock;
			this.scope = scope;
			this.resolver = resolver;
			this.includeRequiredFeatures = includeRequiredFeatures;
			this.includeFstr = includeFstr;
			this.excludeCurrentFeature = excludeCurrentFeature;
		}

		@Override
		public Object visitName(Name node) throws Exception {
			SignatureBuilder builder = new SignatureBuilder();
			return builder.signature(node, containingBlock, scope, resolver,
					includeRequiredFeatures, includeFstr,
					excludeCurrentFeature);
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {

			if (includeRequiredFeatures) {
				/*
				 * if the call target isn't a simple variable name, we can still
				 * use the name of the method being called as a single-item
				 * signature (but not for FSTR as that wouldn't be a dominating
				 * method call).
				 */
				SimpleNode parent = AstParentNodeFinder.findParent(node,
						scope.getAst());
				if (parent instanceof Call) {
					/* TODO: support direct-callability as a constraint */
				} else if (parent instanceof Attribute) {

					SimpleNode grandparent = AstParentNodeFinder.findParent(
							parent, scope.getAst());
					if (grandparent instanceof Call) {
						return Collections.singleton((Call) grandparent);
					}
				}
			}

			return Collections.<Call> emptySet();
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			/* No traversal */
		}

	}
}
