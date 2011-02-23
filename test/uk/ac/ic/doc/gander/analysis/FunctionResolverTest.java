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

import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;

public class FunctionResolverTest {

	// These tests use the name of the parameter as a tag to test if the
	// function resolved is the function expected

	private static final String TEST_FOLDER = "python_test_code/function_resolution";
	private FunctionResolver resolver;
	private Model model;
	private Module module;

	@Test
	public void testLocalNamespace() throws Throwable {
		check("test_local_namespace", "local_tag_expected", "local_tag");
	}

	@Test
	public void testImported() throws Throwable {
		check("test_imported", "love_expected", "love");
	}

	@Test
	public void testImportedFromPackageModule() throws Throwable {
		check("test_imported_from_package_module", "emily_expected", "emily");
	}

	@Test
	public void testImportedFromPackage() throws Throwable {
		check("test_imported_from_package", "music_expected", "music");
	}

	@Test
	public void testImportedFromSubpackageModule() throws Throwable {
		check("test_imported_from_subpackage_module", "werthers_expected",
				"werthers");
	}

	@Before
	public void setup() throws Throwable {
		URL topLevel = getClass().getResource(TEST_FOLDER);

		File topLevelDirectory = new File(topLevel.toURI());

		model = new Model(topLevelDirectory);
		module = model.getTopLevelPackage().getModules().get("resolve");
	}

	/**
	 * Check that the call with the first given tag resolves to a function
	 * taking a parameter whose name matches the second given tag.
	 */
	private void check(String caseName, String callTag, String parameterTag)
			throws Exception {
		Function function = module.getFunctions().get(caseName);

		Call call = new CallFinderByCallTag(callTag, function).getCall();
		assertTrue("Unable to find tagged call in test: '" + callTag + "'",
				call != null);

		resolver = new FunctionResolver(call, function, model);
		assertTrue("Function wasn't resolved: '" + call + "'", resolver
				.getFunction() != null);

		FunctionDef resolvedFunction = resolver.getFunction().getFunctionDef();
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
		private String callTag;

		public CallFinderByCallTag(String callTag, Function function)
				throws Exception {
			this.callTag = callTag;
			function.getFunctionDef().accept(this);
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
