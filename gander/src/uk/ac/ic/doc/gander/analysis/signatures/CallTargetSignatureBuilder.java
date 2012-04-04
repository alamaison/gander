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
			OldNamespace scope, TypeResolver resolver) {
		if (!CallHelper.isIndirectCall(call)) {

			return Collections.emptySet();
		} else {

			return interfaceType(CallHelper.indirectCallTarget(call),
					containingBlock, scope, resolver);
		}
	}

	public Set<Call> interfaceType(exprType expression,
			BasicBlock containingBlock, OldNamespace scope,
			TypeResolver resolver) {

		try {
			return (Set<Call>) expression.accept(new SignatureMapper(
					containingBlock, scope, resolver));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private class SignatureMapper extends VisitorBase {

		private final OldNamespace scope;
		private final BasicBlock containingBlock;
		private final TypeResolver resolver;

		SignatureMapper(BasicBlock containingBlock, OldNamespace scope,
				TypeResolver resolver) {
			this.containingBlock = containingBlock;
			this.scope = scope;
			this.resolver = resolver;
		}

		@Override
		public Object visitName(Name node) throws Exception {
			SignatureBuilder builder = new SignatureBuilder();
			return builder.signature(node, containingBlock, scope, resolver);
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {

			/*
			 * TODO: if the call target isn't a simple variable name, we can
			 * still use the name of the method being called as a single-item
			 * signature
			 */
			SimpleNode parent = AstParentNodeFinder.findParent(node,
					scope.getAst());
			if (parent instanceof Call) {
				/* TODO: support direct-callability as a constraint */
			} else if (parent instanceof Attribute) {

				SimpleNode grandparent = AstParentNodeFinder.findParent(parent,
						scope.getAst());
				if (grandparent instanceof Call) {
					return Collections.singleton((Call) grandparent);
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
