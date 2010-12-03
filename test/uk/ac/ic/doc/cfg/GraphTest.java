/**
 * 
 */
package uk.ac.ic.doc.cfg;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public abstract class GraphTest {
	private Map<String, Collection<String>> links;
	private Set<BasicBlock> blocks;
	private BasicBlock start;
	private BasicBlock end;
	private String name;

	public GraphTest(Map<String, Collection<String>> links,
			Set<BasicBlock> allBlocks, BasicBlock start, BasicBlock end,
			String name) {
		this.links = links;
		this.blocks = allBlocks;
		this.start = start;
		this.end = end;
		this.name = name;
	}

	public void run() {
		check(links, blocks, start, end, name);
	}

	protected abstract boolean areLinked(BasicBlock source,
			BasicBlock target);

	protected abstract BasicBlock tagToBlock(String tag);

	protected abstract boolean selfLinkRequired();

	private void check(Map<String, Collection<String>> expectedLinks,
			Set<BasicBlock> allBlocks, BasicBlock start, BasicBlock end,
			String name) {

		Set<BasicBlock> sourceBlocks = new HashSet<BasicBlock>(); // for
		// non-link test

		// Check links. Each source block should link to its
		// expected target blocks but no other
		for (String sourceTag : expectedLinks.keySet()) {
			BasicBlock source = tagToBlock(sourceTag);
			sourceBlocks.add(source);

			Set<BasicBlock> nonTargetBlocks = new HashSet<BasicBlock>(
					allBlocks);

			for (String targetTag : expectedLinks.get(sourceTag)) {
				BasicBlock target;
				if (targetTag.equals("END"))
					target = end;
				else if (targetTag.equals("START"))
					target = start;
				else
					target = tagToBlock(targetTag);

				assertTrue(name + ": Call to function " + sourceTag
						+ "() must link to call to " + targetTag
						+ "() but analysis says that it doesn't",
						areLinked(source, target));

				nonTargetBlocks.remove(target);
			}

			// Check that any blocks that aren't meant to be targets of
			// anything else, really aren't
			for (BasicBlock block : nonTargetBlocks) {
				boolean hasLink = areLinked(source, block);
				if (selfLinkRequired() && block == source)
					assertTrue(name
							+ ": All blocks must link to themselves",
							hasLink);
				else
					assertFalse(name
							+ ": Block containing call to function "
							+ sourceTag + "() has an unexpected link:\n"
							+ source + " links to " + block, hasLink);
			}

		}

		// Then check that all other blocks (except START/END) are
		// non-dominating
		Set<BasicBlock> nonDomBlocks = new HashSet<BasicBlock>(allBlocks);
		nonDomBlocks.removeAll(sourceBlocks);
		nonDomBlocks.remove(start);
		nonDomBlocks.remove(end);
		for (BasicBlock nonDomBlock : nonDomBlocks) {
			// Shouldn't dominate anything but itself
			for (BasicBlock block : allBlocks) {
				boolean hasLink = areLinked(nonDomBlock, block);
				if (block == nonDomBlock)
					assertTrue("All blocks must " + name
							+ "dominate themselves", hasLink);
				else
					assertFalse("Unexpected " + name + "domination:\n"
							+ nonDomBlock + " " + name + "dominates "
							+ block, hasLink);

			}
		}
	}
}