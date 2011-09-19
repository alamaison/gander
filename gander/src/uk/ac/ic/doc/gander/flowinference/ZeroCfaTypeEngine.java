package uk.ac.ic.doc.gander.flowinference;

import java.util.ArrayList;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.BinOp;
import org.python.pydev.parser.jython.ast.BoolOp;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.Compare;
import org.python.pydev.parser.jython.ast.Comprehension;
import org.python.pydev.parser.jython.ast.Dict;
import org.python.pydev.parser.jython.ast.DictComp;
import org.python.pydev.parser.jython.ast.ExtSlice;
import org.python.pydev.parser.jython.ast.For;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.GeneratorExp;
import org.python.pydev.parser.jython.ast.IfExp;
import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.Index;
import org.python.pydev.parser.jython.ast.Lambda;
import org.python.pydev.parser.jython.ast.List;
import org.python.pydev.parser.jython.ast.ListComp;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.NonLocal;
import org.python.pydev.parser.jython.ast.Num;
import org.python.pydev.parser.jython.ast.Repr;
import org.python.pydev.parser.jython.ast.Set;
import org.python.pydev.parser.jython.ast.SetComp;
import org.python.pydev.parser.jython.ast.Slice;
import org.python.pydev.parser.jython.ast.Starred;
import org.python.pydev.parser.jython.ast.Str;
import org.python.pydev.parser.jython.ast.StrJoin;
import org.python.pydev.parser.jython.ast.Subscript;
import org.python.pydev.parser.jython.ast.TryExcept;
import org.python.pydev.parser.jython.ast.Tuple;
import org.python.pydev.parser.jython.ast.UnaryOp;
import org.python.pydev.parser.jython.ast.Yield;
import org.python.pydev.parser.jython.ast.excepthandlerType;
import org.python.pydev.parser.jython.ast.exprType;
import org.python.pydev.parser.jython.ast.num_typeType;
import org.python.pydev.parser.jython.ast.stmtType;

import uk.ac.ic.doc.gander.ast.BindingStatementVisitor;
import uk.ac.ic.doc.gander.ast.ExpressionVisitor;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.TNamespace;
import uk.ac.ic.doc.gander.flowinference.types.TTop;
import uk.ac.ic.doc.gander.flowinference.types.TUnion;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.CodeBlock;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Member;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.name_binding.Binder;

interface TypeEngine {

	/**
	 * Infer the type of the expression.
	 * 
	 * TODO: Can we get rid of the scope parameter? It shouldn't strictly be
	 * necessary.
	 */
	public Type typeOf(exprType expression, Namespace scope);
}

/**
 * Flow-insensitive, context-insensitive, container-insensitive type inference
 * engine.
 */
public final class ZeroCfaTypeEngine implements TypeEngine {
	private Model model;

	public ZeroCfaTypeEngine(Model model) {
		this.model = model;
	}

	public Type typeOf(exprType expression, Namespace scope) {
		try {
			return (Type) expression
					.accept(new ZeroCfaTypeFinder(model, scope));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}

final class MemberUtils {

	static Type convertMemberToType(Member member) {
		if (member instanceof Module) {
			return new TModule((Module) member);
		} else if (member instanceof Class) {
			return new TClass((Class) member);
		} else if (member instanceof Function) {
			return new TFunction((Function) member);
		} else {
			return new TTop();
		}
	}
}

final class TokenUtils {

	static Type extractTokenTypeFromNamespace(Namespace namespace,
			String tokenName, Model model) {
		Member member = namespace.lookupMember(tokenName);
		if (member != null) {
			return MemberUtils.convertMemberToType(member);
		} else {
			return new AstBasedTokenTypeInferer(model).typeof(tokenName,
					namespace);
		}
	}

}

/**
 * Find the type of an attribute given the inferred type of the target.
 */
final class AttributeTypeLookup {

	private final Model model;

	public AttributeTypeLookup(Model model) {
		this.model = model;
	}

	Type type(Type targetType, NameTok attributeName) {
		if (targetType instanceof TNamespace) {

			// TODO: If we can't find a matching entry in the namespace type
			// definition then we will have to do something more complex to deal
			// with the possibility that it is a field. Or even worse, that it
			// is a method added at runtime. For the moment we return Top (don't
			// know) in that case.
			return TokenUtils.extractTokenTypeFromNamespace(
					((TNamespace) targetType).getNamespaceInstance(),
					attributeName.id, model);

		}
		return new TTop();
	}
}

/**
 * Infer types for tokens by analysing the assignments to variables.
 * 
 * The search is flow, context and container insensitive as it treats the token
 * as a simple string rather than an identifier at a particular location, stack
 * frame, or allocated object.
 */
final class AstBasedTokenTypeInferer {

	private final Model model;

	public AstBasedTokenTypeInferer(Model model) {
		this.model = model;
	}

	/**
	 * Find token by searching local AST for assignments to the same name.
	 * 
	 * XXX: What about the case where the first assignment to the variable is in
	 * a *sibling* scope. For instance, a global variable that is initialised
	 * through an {@code init()} method. This search wouldn't find the
	 * assignment that happens in the body of {@code init()}. This won't give
	 * the *wrong* answer, as we eventually fail to find a typing leading to a
	 * judgement of TTop, unless there is an assignment in an enclosing
	 * namespace *and* in a sibling and these assignments assign different
	 * types. In this case, the inferred type will be too narrow, it will only
	 * include the type in the enclosing namespace rather than leading to a
	 * union type.
	 */
	public Type typeof(final String token, final Namespace enclosingScope) {

		final Namespace scope = new Binder()
				.resolveScope(token, enclosingScope);

		// XXX: What happens if a globalled token and a formal parameter share
		// the same name?

		CodeBlock codeBlock = scope.asCodeBlock();

		// TODO: Somehow infer the type of other formal parameters
		if (codeBlock.getFormalParameters().contains(token)) {

			/*
			 * The first parameter of a function in a class (usually called
			 * self) is always an instance of the class so we can trivially
			 * infer its type
			 */
			if (scope instanceof Function
					&& scope.getParentScope() instanceof Class) {
				if (codeBlock.getFormalParameters().get(0).equals(token)) {
					return new TClass((Class) scope.getParentScope());
				}
			}

			return new TTop();
		}

		// The token may be assigned to in more than one place and these may
		// have different types so we keep them all in order to create a union
		// type if necessary
		final ArrayList<Type> assignedTypes = new ArrayList<Type>();
		try {
			codeBlock.accept(new BindingStatementVisitor() {

				@Override
				public Object visitTryExcept(TryExcept node) throws Exception {
					for (excepthandlerType handler : node.handlers) {
						if (handler.name instanceof Name) {
							if (((Name) handler.name).id.equals(token)) {

								Type exceptionType;
								if (handler.type instanceof Name) {

									// XXX: Very bad! We're not trying to
									// resolve the type expression properly.
									// Instead we look blindly at the top level
									// hoping that any exception will be
									// declared there. Do this right.
									Class exceptionClass = model.getTopLevel()
											.getClasses().get(
													((Name) handler.type).id);
									if (exceptionClass != null) {
										exceptionType = new TClass(
												exceptionClass);
									} else {
										exceptionType = new TTop();
									}
								} else {
									// TODO: Try to resolve the expression to an
									// exception class
									exceptionType = new TTop();
								}

								// If any of the above attempts to convert the
								// declared type to a model class fail, we must
								// add Top as we _have_ found the token, we just
								// don't know its type. Not adding anything
								// would mean we found no binding for that token
								// which would be a lie (and cause the search to
								// continue in the parent scope).
								assignedTypes.add(exceptionType);

								// The exception handler could rebind the
								// exception so we must investigate its body.
								for (stmtType stmt : handler.body) {
									stmt.accept(this);
								}
							}
						} else {
							// XXX: No idea what happens here. How could the
							// name of the exception object _not_ be a name?
						}
					}

					// XXX: Is it possible for the try block to bind one of the
					// handler's exception objects before the handler is
					// reached? It would seem odd but the spec [PEP 227] seems
					// to imply it is. Anyway, let's look for the token in it
					// anyway.
					for (stmtType stmt : node.body) {
						stmt.accept(this);
					}

					return null;
				}

				@Override
				public Object visitImportFrom(ImportFrom node) throws Exception {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Object visitImport(Import node) throws Exception {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Object visitFunctionDef(FunctionDef node)
						throws Exception {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Object visitFor(For node) throws Exception {
					if (node.target instanceof Name
							&& ((Name) node.target).id.equals(token)) {
						// TODO: Try to infer type of iterable
						assignedTypes.add(new TTop());
					}

					// Either the body or the else block may rebind the loop
					// variable so we continue the search there
					for (stmtType stmt : node.body) {
						stmt.accept(this);
					}
					if (node.orelse != null)
						node.orelse.accept(this);

					return null;
				}

				@Override
				public Object visitClassDef(ClassDef node) throws Exception {
					if (((NameTok) node.name).id.equals(token)) {
						Class klass = scope.getClasses().get(
								((NameTok) node.name).id);
						// If we can see the classdef here, it _must_ already be
						// in the model.
						//
						// XXX: Not sure exactly how our model classes and this
						// relate conceptually. We've had this issue before as
						// well. Needs more thought.
						assert klass != null;
						assignedTypes.add(new TClass(klass));
						// FIXME: But we're also using TClass for objects!! so
						// have we just said the token is a class or an object
						// instance of the class?!
					}

					// Do NOT recurse into the ClassDef body. Despite
					// appearances, it is not part of this scope's code object.
					// It is a declaration of the class scope's code object.
					// Another way to think about it: the class's body is not
					// being 'executed' now whereas the enclosing namespace's
					// body is.

					return null;
				}

				@Override
				public Object visitAssign(Assign node) throws Exception {
					// TODO: compute rhs type on demand
					Type rhsType = null;
					for (exprType lhsExpression : node.targets) {

						if (lhsExpression instanceof Name
								&& ((Name) lhsExpression).id.equals(token)) {
							// compute rhs type on demand
							if (rhsType == null) {
								rhsType = (Type) node.value
										.accept(new ZeroCfaTypeFinder(model,
												scope));
								// assert rhsType != null;
							}
							if (rhsType == null) {
								System.err
										.println("Unable to resolve attribute: "
												+ node.value);
							} else {
								assignedTypes.add(rhsType);
							}

						}
					}
					/*
					 * FIXME: If this search is happening on a global, we limit
					 * this search to the enclosing module scope completely
					 * ignoring the fact that another module can assign to the
					 * global by importing the module and referencing the
					 * variable explicitly. We're also ignoring the __builtin__
					 * module.
					 */

					return null;
				}

				@Override
				public void traverse(SimpleNode node) throws Exception {
					// Traverse by default so that we catch all assignments even
					// if they are nested
					node.traverse(this);
				}

				@Override
				protected Object unhandled_node(SimpleNode node)
						throws Exception {
					return null;
				}

			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// If we didn't find any assignments to the token in this scope,
		// this doesn't mean it has unknown type (TTop). It means that,
		// assuming the program is correct, the assignment must be in an
		// enclosing scope.
		if (assignedTypes.isEmpty())
			return null;
		else if (assignedTypes.size() == 1)
			return assignedTypes.get(0);
		else
			return new TUnion(assignedTypes);
	}

}

/**
 * Find the type of a name (a variable) given its enclosing scope.
 */
final class NameTypeLookup {

	private final Model model;

	public NameTypeLookup(Model model) {
		this.model = model;
	}

	public Type type(Name name, Namespace scope) {
		Type type = new AstBasedTokenTypeInferer(model).typeof(name.id, scope);
		if (type != null)
			return type;
		else
			return new TTop();
	}

}

/**
 * Visitor that determines the type of the expression it visits.
 */
final class ZeroCfaTypeFinder extends ExpressionVisitor {

	private final Model model;
	private final Namespace scope;
	private final Type dictType;
	private final TClass listType;
	private final TClass setType;
	private final TClass intType;
	private final TClass longType;
	private final TClass floatType;
	private final TClass strType;
	private final TClass tupleType;
	private final TTop topType;

	public ZeroCfaTypeFinder(Model model, Namespace scope) {
		this.model = model;
		this.scope = scope;
		dictType = new TClass(model.getTopLevel().getClasses().get("dict"));
		listType = new TClass(model.getTopLevel().getClasses().get("list"));
		setType = new TClass(model.getTopLevel().getClasses().get("set"));
		intType = new TClass(model.getTopLevel().getClasses().get("int"));
		longType = new TClass(model.getTopLevel().getClasses().get("long"));
		floatType = new TClass(model.getTopLevel().getClasses().get("float"));
		strType = new TClass(model.getTopLevel().getClasses().get("str"));
		tupleType = new TClass(model.getTopLevel().getClasses().get("tuple"));
		topType = new TTop();
	}

	@Override
	public Object visitList(List node) throws Exception {
		return listType;
	}

	@Override
	public Object visitNum(Num node) throws Exception {
		switch (node.type) {
		case num_typeType.Int:
			return intType;
		case num_typeType.Long:
			return longType;
		case num_typeType.Float:
			return floatType;
		default:
			// TODO: Handle other numeric literal types
			return topType;
		}
	}

	@Override
	public Object visitStr(Str node) throws Exception {
		return strType;
	}

	@Override
	public Object visitAttribute(Attribute node) throws Exception {
		Type targetType = (Type) node.value.accept(this);

		Type type = new AttributeTypeLookup(model).type(targetType,
				(NameTok) node.attr);
		// assert type != null;
		return type;
	}

	@Override
	public Object visitBinOp(BinOp node) throws Exception {
		return topType;
	}

	@Override
	public Object visitBoolOp(BoolOp node) throws Exception {
		return topType;
	}

	@Override
	public Object visitStarred(Starred node) throws Exception {
		return topType;
	}

	@Override
	public Object visitUnaryOp(UnaryOp node) throws Exception {
		return topType;
	}

	@Override
	public Object visitYield(Yield node) throws Exception {
		return topType;
	}

	@Override
	public Object visitCall(Call node) throws Exception {
		return topType;
	}

	@Override
	public Object visitIfExp(IfExp node) throws Exception {
		return topType;
	}

	@Override
	public Object visitCompare(Compare node) throws Exception {
		return topType;
	}

	@Override
	public Object visitComprehension(Comprehension node) throws Exception {
		return topType;
	}

	@Override
	public Object visitDict(Dict node) throws Exception {
		return dictType;
	}

	@Override
	public Object visitDictComp(DictComp node) throws Exception {
		return dictType;
	}

	@Override
	public Object visitExtSlice(ExtSlice node) throws Exception {
		return topType;
	}

	@Override
	public Object visitGeneratorExp(GeneratorExp node) throws Exception {
		return topType;
	}

	@Override
	public Object visitIndex(Index node) throws Exception {
		return topType;
	}

	@Override
	public Object visitLambda(Lambda node) throws Exception {
		return topType;
	}

	@Override
	public Object visitListComp(ListComp node) throws Exception {
		return listType;
	}

	@Override
	public Object visitName(Name node) throws Exception {
		return new NameTypeLookup(model).type(node, scope);
	}

	@Override
	public Object visitNonLocal(NonLocal node) throws Exception {
		return topType;
	}

	@Override
	public Object visitRepr(Repr node) throws Exception {
		return strType;
	}

	@Override
	public Object visitSet(Set node) throws Exception {
		return setType;
	}

	@Override
	public Object visitSetComp(SetComp node) throws Exception {
		return setType;
	}

	@Override
	public Object visitSlice(Slice node) throws Exception {
		return topType;
	}

	@Override
	public Object visitStrJoin(StrJoin node) throws Exception {
		return topType;
	}

	@Override
	public Object visitSubscript(Subscript node) throws Exception {
		return topType;
	}

	@Override
	public Object visitTuple(Tuple node) throws Exception {
		return tupleType;
	}

	@Override
	public void traverse(SimpleNode node) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	protected Object unhandled_node(SimpleNode node) throws Exception {
		return null;
	}
}
