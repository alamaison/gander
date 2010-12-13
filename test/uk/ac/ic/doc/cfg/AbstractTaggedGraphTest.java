package uk.ac.ic.doc.cfg;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.cfg.model.BasicBlock;
import uk.ac.ic.doc.cfg.model.Cfg;

public abstract class AbstractTaggedGraphTest extends GraphTest {

	private Cfg graph;

	public AbstractTaggedGraphTest(String[][] links, Cfg graph, String name) {
		super(links, graph.getBlocks(), graph.getStart(), graph.getEnd(), name);
		this.graph = graph;
	}

	protected BasicBlock findBlockContainingCall(String tag) {

		Collection<BasicBlock> blocks = getBlocks();
		BasicBlock block = null;
		for (BasicBlock b : blocks) {
			if (isBlockTaggedWithFunction(b, tag)) {
				assertTrue("Multiple nodes with same function tag: " + tag
						+ ". This violates the convention we are using for "
						+ "these tests.", block == null);
				block = b;
			}
		}

		return block;
	}

	@Override
	protected final BasicBlock tagToBlock(String tag) {
		if (tag.equals("END"))
			return getEnd();
		else if (tag.equals("START"))
			return getStart();
		else if (tag.equals("EXCEPTION"))
			return graph.getException();
		else
			return findBlockContainingCall(tag);

	}

	@Override
	protected String formatBlock(BasicBlock block) {
		List<String> tags = new ArrayList<String>();
		if (block.statements.isEmpty())
			return block.stringerise();

		try {
			for (SimpleNode node : block.statements) {
				Call call = (Call) node;
				Name funcName = (Name) call.func;
				tags.add(funcName.id);
			}
		} catch (ClassCastException e) {
			return block.toString();
		}
		return tags.toString();
	}

	private static boolean isBlockTaggedWithFunction(BasicBlock block,
			String tag) {
		boolean found = false;
		for (SimpleNode node : block) {
			if (isFunctionNamed(tag, node)) {
				assertFalse(
						"Multiple statements in the block with same function "
								+ "tag: " + tag + ". This violates the "
								+ "convention we are using for these tests.",
						found);
				found = true;
			}
		}
		return found;
	}

	private static boolean isFunctionNamed(String name, SimpleNode node) {
		Call call = (Call) node;
		Name funcName = (Name) call.func;
		return funcName.id.equals(name);
	}
}
