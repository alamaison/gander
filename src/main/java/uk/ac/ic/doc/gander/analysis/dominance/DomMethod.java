/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ic.doc.gander.analysis.dominance;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.cfg.BasicBlock.Visitor;

public class DomMethod {

	private Cfg graph;

	public DomMethod(Cfg graph) {
		this.graph = graph;
	}

	public Iterable<BasicBlock> getBlocks() {
		return graph.getBlocks();
	}

	public BasicBlock getEntryBlock() {
		return graph.getStart();
	}

	public BasicBlock getExitBlock() {
		return graph.getEnd();
	}

	/**
	 * Walks the basic block tree in depth-first order, calling the visitor
	 * method once for every block. This depth-first walk may be run forward
	 * from the method entry point or backwards from the method exit points.
	 * 
	 * @param reverse
	 *            true if this should walk backwards from the exit points
	 * @param v
	 *            {@code non-null;} callback interface. {@code parent} is set
	 *            unless this is the root node
	 */
	public void forEachBlockDepthFirst(boolean reverse, Visitor walker) {
		Set<BasicBlock> visited = new HashSet<BasicBlock>();

		// We push the parent first, then the child on the stack.
		Stack<BasicBlock> stack = new Stack<BasicBlock>();

		BasicBlock rootBlock = reverse ? getExitBlock() : getEntryBlock();

		if (rootBlock == null) {
			// in the case there's no exit block
			return;
		}

		stack.add(null); // Start with null parent.
		stack.add(rootBlock);

		while (stack.size() > 0) {
			BasicBlock cur = stack.pop();
			BasicBlock parent = stack.pop();

			if (!visited.contains(cur)) {
				Iterable<BasicBlock> children = reverse ? cur.getPredecessors()
						: cur.getSuccessors();
				for (BasicBlock block : children) {
					stack.add(cur);
					stack.add(block);
				}
				visited.add(cur);
				walker.visitBlock(cur, parent);
			}
		}
	}

}
