package uk.ac.ic.doc.gander.analysis.signatures;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.CallHelper;
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
		assert CallHelper.isIndirectCall(call);
		if (!CallHelper.isIndirectCall(call))
			return Collections.emptySet();

		exprType target = CallHelper.indirectCallTarget(call);

		if (target instanceof Name) {
			SignatureBuilder builder = new SignatureBuilder();
			return builder.signature(
					(Name) CallHelper.indirectCallTarget(call),
					containingBlock, scope, resolver);
		} else {
			// if the call target isn't a simple variable name, we can still use
			// the name of the method being called as a single-item signature
			Set<Call> degenerateSignature = new HashSet<Call>();
			degenerateSignature.add(call);
			return degenerateSignature;
		}
	}
}
