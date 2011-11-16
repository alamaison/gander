package uk.ac.ic.doc.gander;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.DefaultModel;
import uk.ac.ic.doc.gander.model.MutableModel;
import uk.ac.ic.doc.gander.model.Namespace;

public class CallHelperTest {

	private TypeResolver typer;
	private Call call;
	private Namespace scope;

	@Test
	public void function() throws Throwable {
		setup("function");

		assertFalse(CallHelper.isIndirectCall(call));
		assertFalse(CallHelper.isConstructorCall(call, scope, typer));
		assertFalse(CallHelper.isMethodCall(call, scope, typer));
		assertFalse(CallHelper.isMethodCallOnName(call, scope, typer));
		assertFalse(CallHelper.isCallToSelf((Function) scope, call));
		assertFalse(CallHelper.isExternalMethodCall(call, (Function) scope,
				typer));
		assertFalse(CallHelper.isExternalMethodCallOnName(call,
				(Function) scope, typer));
	}

	@Test
	public void importedFunction() throws Throwable {
		setup("imported_function");

		assertTrue(CallHelper.isIndirectCall(call));
		assertEquals("foo", CallHelper.indirectCallName(call));
		assertFalse(CallHelper.isConstructorCall(call, scope, typer));
		assertFalse(CallHelper.isMethodCall(call, scope, typer));
		assertFalse(CallHelper.isMethodCallOnName(call, scope, typer));
		assertFalse(CallHelper.isCallToSelf((Function) scope, call));
		assertFalse(CallHelper.isExternalMethodCall(call, (Function) scope,
				typer));
		assertFalse(CallHelper.isExternalMethodCallOnName(call,
				(Function) scope, typer));
	}

	@Test
	public void importedAsFunction() throws Throwable {
		setup("imported_as_function");

		assertTrue(CallHelper.isIndirectCall(call));
		assertEquals("foo", CallHelper.indirectCallName(call));
		assertFalse(CallHelper.isConstructorCall(call, scope, typer));
		assertFalse(CallHelper.isMethodCall(call, scope, typer));
		assertFalse(CallHelper.isMethodCallOnName(call, scope, typer));
		assertFalse(CallHelper.isCallToSelf((Function) scope, call));
		assertFalse(CallHelper.isExternalMethodCall(call, (Function) scope,
				typer));
		assertFalse(CallHelper.isExternalMethodCallOnName(call,
				(Function) scope, typer));
	}

	@Test
	public void importedFromFunction() throws Throwable {
		setup("imported_from_function");

		assertFalse(CallHelper.isIndirectCall(call));
		assertFalse(CallHelper.isConstructorCall(call, scope, typer));
		assertFalse(CallHelper.isMethodCall(call, scope, typer));
		assertFalse(CallHelper.isMethodCallOnName(call, scope, typer));
		assertFalse(CallHelper.isCallToSelf((Function) scope, call));
		assertFalse(CallHelper.isExternalMethodCall(call, (Function) scope,
				typer));
		assertFalse(CallHelper.isExternalMethodCallOnName(call,
				(Function) scope, typer));
	}

	@Test
	public void unresolvedImportedFunction() throws Throwable {
		setup("unresolved_imported_function");

		assertTrue(CallHelper.isIndirectCall(call));
		assertEquals("foo", CallHelper.indirectCallName(call));
		assertFalse(CallHelper.isConstructorCall(call, scope, typer));
		assertFalse(CallHelper.isMethodCall(call, scope, typer));
		assertFalse(CallHelper.isMethodCallOnName(call, scope, typer));
		assertFalse(CallHelper.isCallToSelf((Function) scope, call));
		assertFalse(CallHelper.isExternalMethodCall(call, (Function) scope,
				typer));
		assertFalse(CallHelper.isExternalMethodCallOnName(call,
				(Function) scope, typer));
	}

	@Test
	public void methodOnLocalvar() throws Throwable {
		setup("method_on_localvar");

		assertTrue(CallHelper.isIndirectCall(call));
		assertEquals("foo", CallHelper.indirectCallName(call));
		assertFalse(CallHelper.isConstructorCall(call, scope, typer));
		assertTrue(CallHelper.isMethodCall(call, scope, typer));
		assertTrue(CallHelper.isMethodCallOnName(call, scope, typer));
		assertFalse(CallHelper.isCallToSelf((Function) scope, call));
		assertTrue(CallHelper.isExternalMethodCall(call, (Function) scope,
				typer));
		assertTrue(CallHelper.isExternalMethodCallOnName(call,
				(Function) scope, typer));
	}

	@Test
	public void methodOnParameter() throws Throwable {
		setup("method_on_parameter");

		assertTrue(CallHelper.isIndirectCall(call));
		assertEquals("foo", CallHelper.indirectCallName(call));
		assertFalse(CallHelper.isConstructorCall(call, scope, typer));
		assertTrue(CallHelper.isMethodCall(call, scope, typer));
		assertTrue(CallHelper.isMethodCallOnName(call, scope, typer));
		assertFalse(CallHelper.isCallToSelf((Function) scope, call));
		assertTrue(CallHelper.isExternalMethodCall(call, (Function) scope,
				typer));
		assertTrue(CallHelper.isExternalMethodCallOnName(call,
				(Function) scope, typer));
	}

	@Test
	public void methodOnParameterField() throws Throwable {
		setup("method_on_parameter_field");

		assertTrue(CallHelper.isIndirectCall(call));
		assertEquals("foo", CallHelper.indirectCallName(call));
		assertFalse(CallHelper.isConstructorCall(call, scope, typer));
		assertTrue(CallHelper.isMethodCall(call, scope, typer));
		assertFalse(CallHelper.isMethodCallOnName(call, scope, typer));
		assertFalse(CallHelper.isCallToSelf((Function) scope, call));
		assertTrue(CallHelper.isExternalMethodCall(call, (Function) scope,
				typer));
		assertFalse(CallHelper.isExternalMethodCallOnName(call,
				(Function) scope, typer));
	}

	@Test
	public void initCall() throws Throwable {
		setup("init_call");

		assertTrue(CallHelper.isIndirectCall(call));
		assertEquals("__init__", CallHelper.indirectCallName(call));
		assertTrue(CallHelper.isConstructorCall(call, scope, typer));
		assertFalse(CallHelper.isMethodCall(call, scope, typer));
		assertFalse(CallHelper.isMethodCallOnName(call, scope, typer));
		assertFalse(CallHelper.isCallToSelf((Function) scope, call));
		assertFalse(CallHelper.isExternalMethodCall(call, (Function) scope,
				typer));
		assertFalse(CallHelper.isExternalMethodCallOnName(call,
				(Function) scope, typer));
	}

	@Test
	public void initCallOnSelf() throws Throwable {
		setup("init_call_on_self");

		assertTrue(CallHelper.isIndirectCall(call));
		assertEquals("__init__", CallHelper.indirectCallName(call));
		assertTrue(CallHelper.isConstructorCall(call, scope, typer));
		assertFalse(CallHelper.isMethodCall(call, scope, typer));
		assertFalse(CallHelper.isMethodCallOnName(call, scope, typer));
		assertTrue(CallHelper.isCallToSelf((Function) scope, call));
		assertFalse(CallHelper.isExternalMethodCall(call, (Function) scope,
				typer));
		assertFalse(CallHelper.isExternalMethodCallOnName(call,
				(Function) scope, typer));
	}

	@Test
	public void initCallMisleading() throws Throwable {
		setup("init_call_misleading");

		assertFalse(CallHelper.isIndirectCall(call));
		assertFalse(CallHelper.isConstructorCall(call, scope, typer));
		assertFalse(CallHelper.isMethodCall(call, scope, typer));
		assertFalse(CallHelper.isMethodCallOnName(call, scope, typer));
		assertFalse(CallHelper.isCallToSelf((Function) scope, call));
		assertFalse(CallHelper.isExternalMethodCall(call, (Function) scope,
				typer));
		assertFalse(CallHelper.isExternalMethodCallOnName(call,
				(Function) scope, typer));
	}

	@Test
	public void inMethodCall() throws Throwable {
		setup("in_method_call");

		assertTrue(CallHelper.isIndirectCall(call));
		assertEquals("foo", CallHelper.indirectCallName(call));
		assertFalse(CallHelper.isConstructorCall(call, scope, typer));
		assertTrue(CallHelper.isMethodCall(call, scope, typer));
		assertTrue(CallHelper.isMethodCallOnName(call, scope, typer));
		assertFalse(CallHelper.isCallToSelf((Function) scope, call));
		assertTrue(CallHelper.isExternalMethodCall(call, (Function) scope,
				typer));
		assertTrue(CallHelper.isExternalMethodCallOnName(call,
				(Function) scope, typer));
	}

	@Test
	public void inMethodSelfCall() throws Throwable {
		setup("in_method_self_call");

		assertTrue(CallHelper.isIndirectCall(call));
		assertEquals("foo", CallHelper.indirectCallName(call));
		assertFalse(CallHelper.isConstructorCall(call, scope, typer));
		assertTrue(CallHelper.isMethodCall(call, scope, typer));
		assertTrue(CallHelper.isMethodCallOnName(call, scope, typer));
		assertTrue(CallHelper.isCallToSelf((Function) scope, call));
		assertFalse(CallHelper.isExternalMethodCall(call,
				(Function) scope, typer));
		assertFalse(CallHelper.isExternalMethodCallOnName(call,
				(Function) scope, typer));
	}

	private void setup(String projectPath) throws Throwable {
		URL topLevel = getClass().getResource("python_test_code/method_helper");

		File topLevelDirectory = new File(new File(topLevel.toURI()),
				projectPath);

		Hierarchy hierarchy = HierarchyFactory
				.createHierarchy(topLevelDirectory);
		MutableModel model = new DefaultModel(hierarchy);

		Module start = model.loadModule("start");
		typer = new TypeResolver(model);

		TaggedCallAndScopeFinder tagFinder = new TaggedCallAndScopeFinder(
				start, "tag");
		scope = tagFinder.getCallScope();
		call = tagFinder.getTaggedCall();
	}
}
