package uk.ac.ic.doc.gander;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.python.pydev.parser.jython.SimpleNode;

import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.cfg.Cfg;

public class TaggedBlockFinder {

	private Cfg graph;

	private static Pattern taggedCallPattern = Pattern
			.compile("(\\w+)\\.(\\w+)\\(\\\"?(\\w+)\\\"?\\)");

	public TaggedBlockFinder(Cfg graph) {
		this.graph = graph;
	}

	public Statement findTaggedStatement(String variable, String method,
			String tag) throws Exception {
		Set<BasicBlock> blocks = graph.getBlocks();
		for (BasicBlock block : blocks) {
			for (SimpleNode statement : block) {
				TaggedCallFinder finder = new TaggedCallFinder(statement,
						variable, method, tag);
				if (finder.isFound())
					return new Statement(finder.getTaggedCall(), block);
			}
		}

		return null; // statement not found
	}

	public Statement findTaggedStatement(String taggedCall) throws Exception {
		Matcher matcher = match(taggedCall);
		return findTaggedStatement(matcher.group(1), matcher.group(2), matcher
				.group(3));
	}

	public String variableFromTag(String taggedCall) {
		return match(taggedCall).group(1);
	}

	public String methodFromTag(String taggedCall) {
		return match(taggedCall).group(2);
	}

	public String tagFromTag(String taggedCall) {
		return match(taggedCall).group(3);
	}

	protected Matcher match(String taggedCall) {
		Matcher matcher = taggedCallPattern.matcher(taggedCall);
		matcher.find();
		return matcher;
	}
}