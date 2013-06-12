package uk.ac.ic.doc.gander;

import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.TUnresolvedImport;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.OldNamespace;

public class CallHelper {

	public static boolean isMethodCall(Call call, OldNamespace scope,
			TypeResolver typer) {
		// Method calls are always indirect, dereferencing (at least) 'self'
		if (!isIndirectCall(call))
			return false;

		// Explicit calls to constructors are not method calls
		if (isExplicitInitCall(call))
			return false;

		// skip calls to module functions - they look like method calls but
		// we want to treat then differently
		Type callTarget = typer.typeOf(new ModelSite<exprType>(
				indirectCallTarget(call), scope.codeObject()));
		return !(callTarget instanceof TModule || callTarget instanceof TUnresolvedImport);
	}

	public static boolean isMethodCallOnName(Call call, OldNamespace scope,
			TypeResolver typer) {
		return isMethodCall(call, scope, typer)
				&& indirectCallTarget(call) instanceof Name;
	}

	public static boolean isExternalMethodCall(Call call, Function function,
			TypeResolver typer) {
		return isMethodCall(call, function, typer)
				&& !isCallToSelf(function, call);
	}

	public static boolean isExternalMethodCallOnName(Call call,
			Function function, TypeResolver typer) {
		return isExternalMethodCall(call, function, typer)
				&& indirectCallTarget(call) instanceof Name;
	}

	public static boolean isCallToSelf(Function function, Call call) {
		// Must me a call of the form 'self.something()' not 'self()' which,
		// though equally valid, is not a call _to_ self.
		if (!isIndirectCall(call))
			return false;

		// If the receiver of the call isn't a simple variable name then it
		// can't be 'self'.
		// XXX: Well, it could really. What if self had been assigned to some
		// other object (aliased) and the called?
		//
		// x.a = self
		// x.a._private_method()
		//
		// Here, x.a will not be an instance of Name but it is still calling
		// a method of the object via self.
		if (!(indirectCallTarget(call) instanceof Name))
			return false;
		String receiverVariableName = ((Name) indirectCallTarget(call)).id;

		// Self is not necessarily the string 'self'. It can be any valid Python
		// variable name. Therefore we determine what it is by pulling the first
		// argument out of the function specification.
		String selfName = getSelfNameFromFunctionDef(function.getAst());

		if (function.getParentScope() instanceof Class) {
			if (selfName == null) {
				System.err.println("WARNING: method with "
						+ "no object parameter? " + function.getFullName());
			} else if (selfName.equals(receiverVariableName)) {
				return true;
			}
		}

		return false;
	}

	private static String getSelfNameFromFunctionDef(FunctionDef function) {
		if (function.args.args.length < 1)
			return null;

		// How can we ever have a non-name parameter name?
		assert function.args.args[0] instanceof Name;

		return ((Name) function.args.args[0]).id;
	}

	/**
	 * For expression of the form x.y.f(), returns x.y.f.
	 */
	public static exprType callableExpression(Call call) {
		return call.func;
	}

	/**
	 * Call is of the form x.f() rather than f().
	 */
	public static boolean isIndirectCall(Call call) {
		return callableExpression(call) instanceof Attribute;
	}

	/**
	 * Get the name of the callable being called.
	 * 
	 * In other words, for an expression of the form x.f(), returns "f".
	 * 
	 * @throws ClassCastException
	 *             if the call is not an indirect call.
	 */
	public static String indirectCallName(Call call) {
		// Cast to NameTok should be safe as there are no other classes
		// subclasses of NameTokType
		return ((NameTok) ((Attribute) callableExpression(call)).attr).id;

	}

	/**
	 * Get the expression whose attribute is being called.
	 * 
	 * In other words, for an expression of the form x.f(), returns x.
	 * 
	 * @throws ClassCastException
	 *             if the call is not an indirect call.
	 */
	public static exprType indirectCallTarget(Call call) {
		return ((Attribute) callableExpression(call)).value;
	}

	/**
	 * Is the call an explicit invocation of an object's __init__ method.
	 * 
	 * I.e. is it an invocations of the object's class's constructor.
	 * 
	 * XXX: I can see a way that this could go wrong. If __init__ were a free
	 * module function and it were called as 'modulename.__init__' we wouldn't
	 * be able to distinguish this. It would need type inference.
	 */
	public static boolean isExplicitInitCall(Call call) {
		// I can't think of any way to call __init__ as a constructor without
		// accessing an attribute. Either self.__init__, ClassName.__init__ or
		// complex().expr().__init__ are all attributes
		if (!isIndirectCall(call))
			return false;

		return indirectCallName(call).equals("__init__");
	}

	public static boolean isConstructorCall(Call call, OldNamespace scope,
			TypeResolver typer) {
		if (isExplicitInitCall(call))
			return true;

		return typer.typeOf(new ModelSite<exprType>(callableExpression(call),
				scope.codeObject())) instanceof TClass;
	}
}
