/**
 * 
 */
package uk.ac.ic.doc.gander.cfg;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ic.doc.gander.cfg.model.BasicBlock;

public abstract class GraphTest {
	private Map<String, Collection<String>> links;
	private Set<BasicBlock> blocks;
	private BasicBlock start;
	private BasicBlock end;
	private String name;

	public GraphTest(Map<String, Collection<String>> links,
			Set<BasicBlock> blocks, BasicBlock start, BasicBlock end,
			String name) {
		this.links = links;
		this.blocks = blocks;
		this.start = start;
		this.end = end;
		this.name = name;
	}

	public GraphTest(String[][] links, Set<BasicBlock> blocks,
			BasicBlock start, BasicBlock end, String name) {

		// Convert 2D-array with possible multiple 'keys' into map of lists
		this.links = new HashMap<String, Collection<String>>();

		for (String[] d : links) {
			if (!this.links.containsKey(d[0]))
				this.links.put(d[0], new ArrayList<String>());
			this.links.get(d[0]).add(d[1]);
		}
		this.blocks = blocks;
		this.start = start;
		this.end = end;
		this.name = name;
	}

	public void run() {
		check(links, blocks, start, end, name);
	}

	protected abstract boolean areLinked(BasicBlock source, BasicBlock target);

	protected abstract BasicBlock tagToBlock(String tag);

	protected abstract String formatBlock(BasicBlock block);

	protected abstract boolean selfLinkRequired();

	protected abstract Set<BasicBlock> getLinkToAllBlocks();

	protected Set<BasicBlock> getBlocks() {
		return blocks;
	}

	protected BasicBlock getStart() {
		return start;
	}

	protected BasicBlock getEnd() {
		return end;
	}

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

			Set<BasicBlock> nonTargetBlocks = new HashSet<BasicBlock>(allBlocks);

			for (String targetTag : expectedLinks.get(sourceTag)) {
				BasicBlock target = tagToBlock(targetTag);

				assertTrue(name + ": " + sourceTag + " must link to "
						+ targetTag + " but analysis says that it doesn't",
						areLinked(source, target));

				nonTargetBlocks.remove(target);
			}

			// Check that any blocks that aren't meant to be targets of
			// anything else, really aren't
			for (BasicBlock block : nonTargetBlocks) {
				boolean hasLink = areLinked(source, block);
				if (selfLinkRequired() && block == source)
					assertTrue(name + ": All blocks must link to themselves",
							hasLink);
				else
					assertFalse(name + ": Block containing " + sourceTag
							+ " has an unexpected link:\n"
							+ formatBlock(source) + " links to "
							+ formatBlock(block), hasLink);
			}

		}

		// Then check that all other blocks (except, those in our special
		// link-all list) don't link to any others
		Set<BasicBlock> nonSourceBlocks = new HashSet<BasicBlock>(allBlocks);
		nonSourceBlocks.removeAll(sourceBlocks);
		nonSourceBlocks.removeAll(getLinkToAllBlocks());

		for (BasicBlock nonSourceBlock : nonSourceBlocks) {
			for (BasicBlock block : allBlocks) {
				boolean hasLink = areLinked(nonSourceBlock, block);
				if (selfLinkRequired() && block == nonSourceBlock)
					assertTrue(name + ": All blocks must link to themselves",
							hasLink);
				else
					assertFalse(name + ": Unexpected link:\n"
							+ formatBlock(nonSourceBlock) + " links to "
							+ formatBlock(block), hasLink);

			}
		}
	}
}