package uk.ac.ic.doc.gander.flowinference;

import java.util.ArrayList;
import java.util.HashSet;

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
import org.python.pydev.parser.jython.ast.Global;
import org.python.pydev.parser.jython.ast.IfExp;
import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.Index;
import org.python.pydev.parser.jython.ast.Lambda;
import org.python.pydev.parser.jython.ast.List;
import org.python.pydev.parser.jython.ast.ListComp;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.NameTokType;
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
import org.python.pydev.parser.jython.ast.VisitorBase;
import org.python.pydev.parser.jython.ast.Yield;
import org.python.pydev.parser.jython.ast.excepthandlerType;
import org.python.pydev.parser.jython.ast.exprType;
import org.python.pydev.parser.jython.ast.num_typeType;
import org.python.pydev.parser.jython.ast.stmtType;

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
import uk.ac.ic.doc.gander.model.LexicalTokenResolver;
import uk.ac.ic.doc.gander.model.Member;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;

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

/**
 * Force subclass to handle every expression type.
 */
abstract class ExpressionVisitor extends VisitorBase {

	@Override
	public abstract Object visitDict(Dict node) throws Exception;

	@Override
	public abstract Object visitList(List node) throws Exception;

	@Override
	public abstract Object visitNum(Num node) throws Exception;

	@Override
	public abstract Object visitSet(Set node) throws Exception;

	@Override
	public abstract Object visitStr(Str node) throws Exception;

	@Override
	public abstract Object visitAttribute(Attribute node) throws Exception;

	@Override
	public abstract Object visitBinOp(BinOp node) throws Exception;

	@Override
	public abstract Object visitBoolOp(BoolOp node) throws Exception;

	@Override
	public abstract Object visitStarred(Starred node) throws Exception;

	@Override
	public abstract Object visitUnaryOp(UnaryOp node) throws Exception;

	@Override
	public abstract Object visitYield(Yield node) throws Exception;

	@Override
	public abstract Object visitCall(Call node) throws Exception;

	@Override
	public abstract Object visitIfExp(IfExp node) throws Exception;

	@Override
	public abstract Object visitCompare(Compare node) throws Exception;

	@Override
	public abstract Object visitComprehension(Comprehension node)
			throws Exception;

	@Override
	public abstract Object visitDictComp(DictComp node) throws Exception;

	@Override
	public abstract Object visitExtSlice(ExtSlice node) throws Exception;

	@Override
	public abstract Object visitGeneratorExp(GeneratorExp node)
			throws Exception;

	@Override
	public abstract Object visitIndex(Index node) throws Exception;

	@Override
	public abstract Object visitLambda(Lambda node) throws Exception;

	@Override
	public abstract Object visitListComp(ListComp node) throws Exception;

	@Override
	public abstract Object visitName(Name node) throws Exception;

	@Override
	public abstract Object visitNonLocal(NonLocal node) throws Exception;

	@Override
	public abstract Object visitRepr(Repr node) throws Exception;

	@Override
	public abstract Object visitSetComp(SetComp node) throws Exception;

	@Override
	public abstract Object visitSlice(Slice node) throws Exception;

	@Override
	public abstract Object visitStrJoin(StrJoin node) throws Exception;

	@Override
	public abstract Object visitSubscript(Subscript node) throws Exception;

	@Override
	public abstract Object visitTuple(Tuple node) throws Exception;
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
			return new AstBasedTokenTypeInferer(model).resolveToken(tokenName,
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
 * Visitor that handles statements that can bind a name.
 * 
 * From PEP227: The following operations are name binding operations. If they
 * occur within a block, they introduce new local names in the current block
 * unless there is also a global declaration.
 * 
 * Function definition: def name ...
 * 
 * Argument declaration: def f(...name...), lambda ...name...
 * 
 * Class definition: class name ...
 * 
 * Assignment statement: name = ...
 * 
 * Import statement: import name, import module as name, from module import name
 * 
 * Implicit assignment: names are bound by for statements and except clauses
 * 
 */
abstract class BindingStatementVisitor extends VisitorBase {

	/**
	 * This is triggered when a function is defined.
	 * 
	 * e.g:
	 * 
	 * <pre>
	 * def fun(x):
	 *     pass
	 * </pre>
	 */
	@Override
	public abstract Object visitFunctionDef(FunctionDef node) throws Exception;

	@Override
	public abstract Object visitAssign(Assign node) throws Exception;

	@Override
	public abstract Object visitClassDef(ClassDef node) throws Exception;

	@Override
	public abstract Object visitFor(For node) throws Exception;

	@Override
	public abstract Object visitImport(Import node) throws Exception;

	@Override
	public abstract Object visitImportFrom(ImportFrom node) throws Exception;

	@Override
	public abstract Object visitTryExcept(TryExcept node) throws Exception;

}

/**
 * Visitor that handles any statement that starts a new code block.
 */
abstract class CodeBlockCreationVisitor extends VisitorBase {

	@Override
	public abstract Object visitClassDef(ClassDef node) throws Exception;

	@Override
	public abstract Object visitFunctionDef(FunctionDef node) throws Exception;

	@Override
	public abstract Object visitModule(
			org.python.pydev.parser.jython.ast.Module node) throws Exception;

	// TODO: Investigate if lambdas belong here

}

/**
 * Visitor that only visits statements that are logically part of the local code
 * block as defined by the Python spec (section 4).
 * 
 * The AST associated with a code block can contain nodes that begin a new code
 * block. When the task at hand only makes sense in the context of the current
 * code block, subclass this class to ensure the analysis doesn't traverse into
 * another code block.
 * 
 * Note, a subclass of this visitor won't even see the class/function
 * declarations in the current scope. A subclasses that needs this should
 * subclass {@link CodeBlockCreationVisitor} and override {@code visitClassDef}
 * or {@code visitFunctionDef} but not traverse their node bodies.
 */
abstract class LocalCodeBlockVisitor extends CodeBlockCreationVisitor {

	@Override
	public final Object visitClassDef(ClassDef node) throws Exception {
		return null;
	}

	@Override
	public final Object visitFunctionDef(FunctionDef node) throws Exception {
		return null;
	}

	@Override
	public final Object visitModule(
			org.python.pydev.parser.jython.ast.Module node) throws Exception {
		return null;
	}

}

/**
 * Infer types for tokens by analysing the assignments to variables.
 * 
 * The search is flow, context and container insensitive as it treats the token
 * as a simple string rather than an identifier at a particular location, stack
 * frame, or allocated object.
 */
final class AstBasedTokenTypeInferer extends LexicalTokenResolver<Type> {

	private final Model model;

	public AstBasedTokenTypeInferer(Model model) {
		this.model = model;
	}

	/**
	 * Return the global namespace for the local scope.
	 * 
	 * In Python there is no truly 'global' namespace (other than __builtin__).
	 * Instead the global namespace is simply the namespace of the module
	 * containing the current code block.
	 * 
	 * XXX: What if the current code block is a module? For the moment we return
	 * the current code block itself.
	 * 
	 * XXX: Does the search look into a module's parent module (i.e. package)?
	 */
	private static Namespace getGlobalNamespace(Namespace localScope) {
		Namespace scope = localScope;
		while (!(scope instanceof Module)) {
			scope = scope.getParentScope();
		}

		assert scope instanceof Module;
		return scope;
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
	@Override
	protected Type searchScopeForToken(final String token, final Namespace scope) {

		// Recursion guard

		// FIXME: This feels like a hack. There must be a better way
		if (!scope.equals(getGlobalNamespace(scope))) {
			/*
			 * The 'global' statement can occur at any point in a code block but
			 * it's effect covers the entire block (the spec says it must appear
			 * before the name is used but CPython only generates a warning).
			 * Therefore we search for the global statement first so that we
			 * don't have to contradict the type we infer below if we come
			 * across a global statement after we've already bound the name
			 * locally
			 */
			Type globalType = lookForGlobalDeclarationOfToken(token, scope);
			if (globalType != null) {
				return globalType;
			}
		}

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
								assert rhsType != null;
							}
							assignedTypes.add(rhsType);

						}
					}

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

	/**
	 * Look for any global declaration of the given name that affect its binding
	 * in the local scope.
	 * 
	 * There are two aspects to this search.
	 * 
	 * Firstly we must decide whether there exists any global declaration that
	 * affect the local scope's binding. This is the case if the global keyword
	 * appears in the local scope or in any of its parent scopes. First we
	 * search the local scope for the global keyword and, failing that, a local
	 * name binding. If we don't find either, we search successive enclosing
	 * namespaces until we find one or reach the global namespace in which case
	 * the name is global by definition.
	 * 
	 * We don't need to search any further out than the enclosing module (global
	 * namespace) as global declarations don't cross module boundaries.
	 * 
	 * XXX: Is this true? What about submodules (subpackages really).
	 * 
	 * Secondly, if we found the name in question to be global in the local
	 * scope, we need to establish its type by searching all its bindings.
	 * 
	 * FIXME: We limit this search to the enclosing module scope completely
	 * ignoring the fact that another module can assign to the global by
	 * importing the module and referencing the variable explicitly. We're also
	 * ignoring the __builtin__ module.
	 * 
	 * @param token
	 *            Name being bound.
	 * @param scope
	 *            Local (current) scope.
	 * @return The inferred type of the global binding if any global binding
	 *         exists, otherwise {@code null}.
	 */
	private Type lookForGlobalDeclarationOfToken(final String token,
			final Namespace scope) {
		final boolean tokenIsGlobal = isNameGlobal(token, scope);

		if (tokenIsGlobal) {
			return TokenUtils.extractTokenTypeFromNamespace(
					getGlobalNamespace(scope), token, model);
		} else {
			return null;
		}
	}

	/**
	 * Determines if the given name binds globally in the local scope.
	 * 
	 * A 'global' statement anywhere in the local scope will cause the name to
	 * be global. Otherwise, a name bound anywhere in the local code block will
	 * not be global.
	 * 
	 * If the name is not bound locally nor locally declared to be global, the
	 * decision falls to the enclosing namespace. This recursion continues,
	 * trying each successive parent, until the name is determined to be local
	 * or global or until the we reach the global namespace. At this point all
	 * names are, by definition global.
	 * 
	 * @param name
	 *            Name being bound.
	 * @param scope
	 *            Scope of the binding.
	 */
	private boolean isNameGlobal(final String name, Namespace scope) {
		if (scope.equals(getGlobalNamespace(scope))) {
			/* TODO: Nasty - shouldn't recompute every time */
			return true;

		} else if (isNameDeclaredGlobalInScope(name, scope)) {
			return true;

		} else if (isNameBoundLocally(name, scope)) {
			return false;

		} else {
			return isNameGlobal(name, scope.getParentScope());
		}
	}

	private boolean isNameBoundLocally(final String name, final Namespace scope) {

		/*
		 * TODO: decide whether we want to search for one name or all names.
		 * They have different performance trade-offs depending on the
		 * application.
		 */

		final java.util.Set<String> boundNames = new HashSet<String>();

		try {

			/*
			 * It might be possible to do this just by overriding visitName and
			 * visitNameTok and looking at their contexts to decide if they are
			 * being used in a binding context but, for the moment, we do it the
			 * long way
			 */

			/*
			 * After extracting the bound names at each node we traverse the
			 * node because, if they have a body like a for-loop, they may nest
			 * other definitions.
			 */
			scope.asCodeBlock().accept(new BindingStatementVisitor() {

				@Override
				public Object visitTryExcept(TryExcept node) throws Exception {
					for (excepthandlerType handler : node.handlers) {
						if (handler.name instanceof Name) {
							boundNames.add(((Name) handler.name).id);
						} else {
							// XXX: No idea what happens here. How could the
							// name of the exception object _not_ be a name?
						}
					}

					node.traverse(this);
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
					if (node.target instanceof Name) {
						boundNames.add(((Name) node.target).id);
					} else {
						// XXX: No idea what happens here. How could the
						// for-loop variable _not_ be a name?
					}

					node.traverse(this);
					return null;
				}

				@Override
				public Object visitClassDef(ClassDef node) throws Exception {
					if (node.name instanceof NameTok) {
						boundNames.add(((NameTok) node.name).id);
					} else {
						// XXX: No idea what happens here. How could the
						// class name _not_ be a name?
					}

					// Do NOT recurse into the ClassDef body. Despite
					// appearances, it is not part of this scope's code block.
					// It is a declaration of the class scope's code block.
					// Another way to think about it: the class's body is not
					// being 'executed' now whereas the enclosing namespace's
					// body is.

					return null;
				}

				@Override
				public Object visitAssign(Assign node) throws Exception {
					for (exprType lhsExpression : node.targets) {
						if (lhsExpression instanceof Name) {
							boundNames.add(((Name) lhsExpression).id);
						}
					}

					node.traverse(this);
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

		return boundNames.contains(name);
	}

	/**
	 * Determines if the given name is declared global in the local scope.
	 * 
	 * This only returns {@code true} if the given scope declares the name to be
	 * global. The name may <em>be</em> globally binding if a parent scope
	 * declares it so but this method only looks at the given scope. If it is
	 * declared global in a nested scope that also won't affect the result of
	 * this method as these declarations don't affect global binding for their
	 * parents.
	 */
	private boolean isNameDeclaredGlobalInScope(final String name,
			final Namespace scope) {

		// Declared in this bizarre way so the anonymous inner can assign to it
		final boolean[] tokenIsGlobal = { false };

		try {

			/*
			 * Using a LocalCodeBlock visitor as a global statement in a nested
			 * class or function doesn't affect the enclosing scope's binding
			 * 
			 * The moment we find the first matching global declaration, it is
			 * time to finish as any others are redundant. We can't return
			 * prematurely from the visitor so we use a flag to know we're
			 * finished and short-cut the visitor's work.
			 */
			scope.asCodeBlock().accept(new LocalCodeBlockVisitor() {

				@Override
				public Object visitGlobal(Global node) throws Exception {
					if (!tokenIsGlobal[0]) {
						for (NameTokType tok : node.names) {
							if (((NameTok) tok).id.equals(name)) {
								tokenIsGlobal[0] = true;
								break;
							}
						}
					}
					return null;
				}

				@Override
				protected Object unhandled_node(SimpleNode node)
						throws Exception {
					return null;
				}

				@Override
				public void traverse(SimpleNode node) throws Exception {
					/*
					 * traverse by default because the 'global' statement might
					 * be nested, for instance, in a loop or conditional
					 */
					if (!tokenIsGlobal[0]) {
						node.traverse(this);
					}
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return tokenIsGlobal[0];
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
		Type type = new AstBasedTokenTypeInferer(model).resolveToken(name.id,
				scope);
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
		assert type != null;
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
