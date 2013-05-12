package uk.ac.ic.doc.gander.ducktype;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.CallHelper;

public class SignatureHelper {

	public static Set<String> convertSignatureToMethodNames(Set<Call> signature) {
		Set<String> methods = new HashSet<String>();

		for (Call c : signature)
			methods.add(CallHelper.indirectCallName(c));

		return methods;
	}
}
