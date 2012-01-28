package uk.ac.ic.doc.gander.flowinference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ScopedAstNode;
import uk.ac.ic.doc.gander.ScopedPrintNode;
import uk.ac.ic.doc.gander.TaggedNodeAndScopeFinder;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Processor;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.MutableModel;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.FunctionCO;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;
import uk.ac.ic.doc.gander.model.codeobject.NestedCodeObject;
import uk.ac.ic.doc.gander.model.codeobject.NestedCodeObjects;

final class TestModule {

	private final String moduleName;
	private final MutableModel model;
	private final ModuleCO module;

	TestModule(String name, MutableModel model) throws Throwable {
		this.moduleName = name;
		this.model = model;

		Module module = model.load(moduleName);
		if (module == null) {
			throw new RuntimeException("Test module not found: " + moduleName);
		} else {
			this.module = module.codeObject();
		}
	}

	ScopedPrintNode printNode(String tag) throws Throwable {
		return ScopedPrintNode.findPrintNode(model, moduleName, tag);
	}

	ModelSite<exprType> taggedExpression(String tag) throws Throwable {
		ScopedAstNode node = new TaggedNodeAndScopeFinder(codeObject(), tag)
				.getTaggedNode();
		assertTrue("Unable to find node tagged with '" + tag + "'",
				node != null);
		return new ModelSite<exprType>((exprType) node.getNode(),
				node.getScope());
	}

	ScopedAstNode taggedNode(String tag) throws Throwable {
		ScopedAstNode node = new TaggedNodeAndScopeFinder(codeObject(), tag)
				.getTaggedNode();
		assertTrue("Unable to find node tagged with '" + tag + "'",
				node != null);
		return node;
	}

	Set<ModelSite<exprType>> printables(String... expressionTags)
			throws Throwable {

		Set<ModelSite<exprType>> expressions = new HashSet<ModelSite<exprType>>();

		for (String tag : expressionTags) {
			expressions.add(printNode(tag).site());
		}

		return expressions;
	}

	Set<ModelSite<exprType>> expressions(String... expressionTags)
			throws Throwable {

		Set<ModelSite<exprType>> expressions = new HashSet<ModelSite<exprType>>();

		for (String tag : expressionTags) {
			expressions.add(taggedExpression(tag));
		}

		return expressions;
	}

	ClassCO builtinClass(String name) {
		return nestedClass(model.getTopLevel().codeObject(), name);
	}

	ClassCO moduleLevelClass(String name) throws Throwable {
		return nestedClass(codeObject(), name);
	}

	FunctionCO moduleLevelFunction(String name) throws Throwable {
		return nestedFunction(codeObject(), name);
	}

	static <T extends exprType, U extends exprType> void assertResultIncludes(
			final String message, final Set<ModelSite<T>> expressions,
			Result<ModelSite<U>> result) {

		result.actOnResult(new Processor<ModelSite<U>>() {

			@Override
			public void processInfiniteResult() {
				fail(message + ". Result is Top.");
			}

			@Override
			public void processFiniteResult(Set<ModelSite<U>> result) {
				Set<ModelSite<T>> missing = new HashSet<ModelSite<T>>(
						expressions);
				missing.removeAll(result);
				assertTrue(message
						+ " Not all expected expressions are present "
						+ "in the result. Missing: " + missing + ". Actual: "
						+ result + ".", missing.isEmpty());
			}
		});
	}

	static <T extends exprType, U extends exprType> void assertResultExcludes(
			String message, final Set<ModelSite<T>> expressions,
			Result<ModelSite<U>> result) {
		assertTrue(
				message,
				result.transformResult(
						new Transformer<ModelSite<U>, Boolean>() {

							@Override
							public Boolean transformFiniteResult(
									Set<ModelSite<U>> result) {
								Set<ModelSite<U>> intersection = new HashSet<ModelSite<U>>(
										result);
								intersection.retainAll(expressions);

								return intersection.isEmpty();
							}

							@Override
							public Boolean transformInfiniteResult() {
								return false;
							}
						}).booleanValue());
	}

	private ModuleCO codeObject() throws Throwable {
		return module;
	}

	private static FunctionCO nestedFunction(CodeObject parent, String name) {
		CodeObject codeObject = nestedObjectHelper(parent, name, "function");
		assertTrue("Found a top-level declaration called '" + name
				+ "' but it's the wrong type.",
				codeObject instanceof FunctionCO);

		return (FunctionCO) codeObject;
	}

	private static ClassCO nestedClass(CodeObject parent, String name) {
		CodeObject codeObject = nestedObjectHelper(parent, name, "class");
		assertTrue("Found a top-level declaration called '" + name
				+ "' but it's the wrong type.", codeObject instanceof ClassCO);

		return (ClassCO) codeObject;
	}

	private static NestedCodeObject nestedObjectHelper(CodeObject parent,
			String name, String lookupType) {

		NestedCodeObjects codeObjects = parent.nestedCodeObjects()
				.namedCodeObjectsDeclaredAs(name);
		assertFalse("No object declared as '" + name + "'.",
				codeObjects.isEmpty());
		assertEquals("Test error: assuming a unique " + lookupType
				+ " declared as '" + name + "' but there are several.", 1,
				codeObjects.size());

		return codeObjects.iterator().next();
	}
}