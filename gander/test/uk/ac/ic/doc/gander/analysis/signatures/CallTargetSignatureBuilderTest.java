package uk.ac.ic.doc.gander.analysis.signatures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.CallHelper;
import uk.ac.ic.doc.gander.TaggedCallAndScopeFinder;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.DefaultModel;
import uk.ac.ic.doc.gander.model.MutableModel;
import uk.ac.ic.doc.gander.model.OldNamespace;

public class CallTargetSignatureBuilderTest {

	private CallTargetSignatureBuilder analyser = new CallTargetSignatureBuilder();
	private TypeResolver typer;
	private OldNamespace scope;
	private Call call;

	private void setup(String projectPath) throws Throwable {
		URL topLevel = getClass().getResource(
				"python_test_code/call_target_signature");

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

	@Test
	public void variable() throws Throwable {
		setup("variable");
		checkSignature("a", "b", "c");
	}

	@Test
	public void attribute() throws Throwable {
		setup("attribute");
		checkSignature("b");
	}

	@Test
	public void callResult() throws Throwable {
		setup("call_result");
		checkSignature("b");
	}

	private void checkSignature(String... names) throws Exception {
		Set<String> expected = new HashSet<String>();
		for (String name : names)
			expected.add(name);

		checkSignature(expected);
	}

	private void checkSignature(Set<String> expected) throws Exception {

		BasicBlock block = findBlockContainingCall(call, scope);

		Set<Call> signature = analyser.signatureOfTarget(call, block, scope,
				typer);

		// Test that all expected calls are in the chain and no unexpected calls
		// are in the chain.
		// TODO: We consider any call with matching name but ignore arguments
		Set<String> calledMethods = calledMethodsFromChain(signature);
		assertEquals("Expected signature doesn't match method found in the "
				+ "signature produced by the analyser", expected, calledMethods);
	}

	private BasicBlock findBlockContainingCall(Call call, OldNamespace scope) {
		for (BasicBlock block : scope.getCfg().getBlocks()) {
			for (SimpleNode node : block) {
				if (node.equals(call))
					return block;
			}
		}

		fail("No block found containing call - this should be impossible");
		return null;
	}

	private Set<String> calledMethodsFromChain(Iterable<Call> chain) {
		Set<String> methods = new HashSet<String>();
		for (Call call : chain) {
			methods.add(CallHelper.indirectCallName(call));
		}
		return methods;
	}
}
