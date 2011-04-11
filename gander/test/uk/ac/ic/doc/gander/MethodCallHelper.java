package uk.ac.ic.doc.gander;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;

public class MethodCallHelper {

	public static NameTok extractMethodCallName(Call call) {
		Attribute fieldAccess = (Attribute) call.func;
		return (NameTok) fieldAccess.attr;
	}

	public static Name extractMethodCallTarget(Call call) {
		Attribute fieldAccess = (Attribute) call.func;
		return (Name) fieldAccess.value;
	}

	static boolean isMethodCall(String variable, String method, Call call) {
		try {
			return matchMethodCall(variable, method, call) != null;
		} catch (ClassCastException e) {
			return false;
		}
	}

	static boolean isMethodCallName(String method, Call call) {
		try {
			return matchMethodCallName(method, call) != null;
		} catch (ClassCastException e) {
			return false;
		}
	}

	static boolean isMethodCallTarget(String variable, Call call) {
		try {
			return matchMethodCallTarget(variable, call) != null;
		} catch (ClassCastException e) {
			return false;
		}
	}

	static NameTok matchMethodCallName(String methodName, Call call) {
		NameTok name = extractMethodCallName(call);
		if (name.id.equals(methodName))
			return name;
		else
			return null;
	}

	static Name matchMethodCallTarget(String variable, Call call) {
		Name target = extractMethodCallTarget(call);
		if (target.id.equals(variable))
			return target;
		else
			return null;
	}

	/**
	 * Converts given statement into method call if it matches the given
	 * criteria.
	 * 
	 * @param variable
	 *            Expected variable name.
	 * @param method
	 *            Expected method name.
	 * @param statement
	 *            Statement to convert.
	 * @return statement converted to a Call if it matches.
	 * @throws ClassCastException
	 *             if the statement isn't a method call.
	 */
	static Call matchMethodCall(String variable, String method,
			SimpleNode statement) {
		Call call = (Call) statement;
		if (isMethodCallTarget(variable, call)
				&& isMethodCallName(method, call))
			return call;
		else
			return null; // call doesn't match criteria
	}
}