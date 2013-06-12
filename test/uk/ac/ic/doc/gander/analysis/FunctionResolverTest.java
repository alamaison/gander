package uk.ac.ic.doc.gander.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.Str;
import org.python.pydev.parser.jython.ast.VisitorBase;

import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.ZeroCfaTypeEngine;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;
import uk.ac.ic.doc.gander.model.DefaultModel;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.MutableModel;

public class FunctionResolverTest {

	// These tests use the name of the parameter as a tag to test if the
	// function resolved is the function expected

	private static final String TEST_FOLDER = "python_test_code/function_resolution";
	private FunctionResolver resolver;
	private MutableModel model;
	private Module module;

	@Test
	public void localNamespace() throws Throwable {
		check("test_local_namespace", "local_tag_expected", "local_tag");
	}

	@Test
	public void imported() throws Throwable {
		check("test_imported", "love_expected", "love");
	}

	@Test
	public void importedFromPackageModule() throws Throwable {
		check("test_imported_from_package_module", "emily_expected", "emily");
	}

	@Test
	public void importedFromPackage() throws Throwable {
		check("test_imported_from_package", "music_expected", "music");
	}

	@Test
	public void importedFromSubpackageModule() throws Throwable {
		check("test_imported_from_subpackage_module", "werthers_expected",
				"werthers");
	}

	@Test
	public void fromImport() throws Throwable {
		check("test_from_import", "cold_and_wet_expected", "cold_and_wet");
	}

	@Test
	public void builtin() throws Throwable {
		check("test_call_builtin", "iterable_expected", "iterable");
	}

	@Test
	public void resolutionInMethod() throws Throwable {
		checkMethod("ClassScope", "test_resolution_in_method",
				"scope_tag_expected", "scope_tag");
	}

	@Test
	public void resolutionInMethodWithClashingMethodName() throws Throwable {
		checkMethod("ClassScope",
				"test_resolution_in_method_with_clashing_method_name",
				"module_tag_expected", "module_tag");
	}

	@Test
	public void callViaNestedImport() throws Throwable {
		check("test_call_via_nested_import", "brother_keeper_expected",
				"brother_keeper_tag");
	}

	@Before
	public void setup() throws Throwable {
		URL topLevel = getClass().getResource(TEST_FOLDER);

		File topLevelDirectory = new File(topLevel.toURI());

		Hierarchy hierarchy = HierarchyFactory
				.createHierarchy(topLevelDirectory);
		model = new DefaultModel(hierarchy);
		module = model.loadModule("resolve");
	}

	/**
	 * Check that the call with the first given tag resolves to a function
	 * taking a parameter whose name matches the second given tag.
	 */
	private void check(String caseName, String callTag, String parameterTag)
			throws Exception {
		Function function = module.getFunctions().get(caseName);

		doCheck(callTag, parameterTag, function);
	}

	private void checkMethod(String className, String caseName, String callTag,
			String parameterTag) throws Exception {

		Function function = module.getClasses().get(className).getFunctions()
				.get(caseName);

		doCheck(callTag, parameterTag, function);
	}

	private void doCheck(String callTag, String parameterTag, Function function)
			throws Exception {
		Call call = new CallFinderByCallTag(callTag, function).getCall();
		assertTrue("Unable to find tagged call in test: '" + callTag + "'",
				call != null);

		resolver = new FunctionResolver(call, function, new TypeResolver(
				new ZeroCfaTypeEngine()));
		assertTrue("Function wasn't resolved: '" + call + "'",
				resolver.getFunction() != null);

		FunctionDef resolvedFunction = resolver.getFunction().getAst();
		assertEquals("Resolved function takes more than one parameter.  This "
				+ "is unexpected.", 1, resolvedFunction.args.args.length);

		assertEquals("Resolved function parameter tag mismatch", parameterTag,
				((Name) resolvedFunction.args.args[0]).id);
	}

	/**
	 * Find first call made by the function that has the given tag.
	 * 
	 * The call must be passed the tag as a Python string literal and it must be
	 * the only parameter passed.
	 */
	private static class CallFinderByCallTag extends VisitorBase {
		private Call call = null;
		private final String callTag;

		public CallFinderByCallTag(String callTag, Function function)
				throws Exception {
			this.callTag = callTag;
			function.getAst().accept(this);
		}

		public Call getCall() {
			return call;
		}

		@Override
		public Object visitCall(Call node) throws Exception {
			if (node.args.length == 1 && node.args[0] instanceof Str) {
				if (((Str) node.args[0]).s.equals(callTag))
					call = node;
			}
			return null;
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			if (call == null)
				node.traverse(this);
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {
			return null;
		}
	}
}
