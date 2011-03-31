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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ic.doc.gander.analysis.dominance.DomMethod;
import uk.ac.ic.doc.gander.cfg.model.BasicBlock;

/**
 * Calculates the dominance-frontiers of a methot's basic blocks. Algorithm from
 * "A Simple, Fast Dominance Algorithm" by Cooper, Harvey, and Kennedy;
 * transliterated to Java.
 */
public class DomFront {
	/** local debug flag */
	private static boolean DEBUG = false;

	/** {@code non-null;} method being processed */
	private final DomMethod meth;

	private final Iterable<BasicBlock> nodes;

	private final Map<BasicBlock, DomInfo> domInfos;

	/**
	 * Dominance-frontier information for a single basic block.
	 */
	public static class DomInfo {
		/**
		 * {@code null-ok;} the dominance frontier set indexed by block index
		 */
		public Set<BasicBlock> dominanceFrontiers;

		/** {@code >= 0 after run();} the index of the immediate dominator */
		public BasicBlock idom = null;

		@Override
		public String toString() {
			return dominanceFrontiers.toString();
		}
	}

	/**
	 * Constructs instance. Call {@link DomFront#run} to process.
	 * 
	 * @param meth
	 *            {@code non-null;} method to process
	 */
	public DomFront(DomMethod meth) {
		this.meth = meth;
		nodes = meth.getBlocks();

		domInfos = new HashMap<BasicBlock, DomInfo>();

		for (BasicBlock block : nodes) {
			domInfos.put(block, new DomInfo());
		}
	}

	/**
	 * Calculates the dominance frontier information for the method.
	 * 
	 * @return {@code non-null;} an array of DomInfo structures
	 */
	public Map<BasicBlock, DomInfo> run() {

		if (DEBUG) {
			for (BasicBlock block : nodes) {
				System.out.println("pred[" + block + "]: "
						+ block.getPredecessors());
			}
		}

		Dominators methDom = Dominators.make(meth, domInfos, false);

		if (DEBUG) {
			for (BasicBlock block : nodes) {
				DomInfo info = domInfos.get(block);
				System.out.println("idom[" + block + "]: " + info.idom);
			}
		}

		for (BasicBlock block : nodes) {
			domInfos.get(block).dominanceFrontiers = new HashSet<BasicBlock>();
			// = SetFactory.makeDomFrontSet(szNodes);
		}

		calcDomFronts();

		if (DEBUG) {
			for (BasicBlock block : nodes) {
				System.out.println("df[" + block + "]: "
						+ domInfos.get(block).dominanceFrontiers);
			}
		}

		return domInfos;
	}

	/**
	 * Calculates the dominance-frontier set. from
	 * "A Simple, Fast Dominance Algorithm" by Cooper, Harvey, and Kennedy;
	 * transliterated to Java.
	 */
	private void calcDomFronts() {
		for (BasicBlock nb : nodes) {
			DomInfo nbInfo = domInfos.get(nb);
			Collection<BasicBlock> pred = nb.getPredecessors();

			if (pred.size() > 1) {
				for (BasicBlock runner : pred) {

					while (runner != nbInfo.idom) {
						/*
						 * We can stop if we hit a block we already added label
						 * to, since we must be at a part of the dom tree we
						 * have seen before.
						 */
						if (runner == null)
							break;

						DomInfo runnerInfo = domInfos.get(runner);

						if (runnerInfo.dominanceFrontiers.contains(nb)) {
							break;
						}

						// Add b to runner's dominance frontier set.
						runnerInfo.dominanceFrontiers.add(nb);
						runner = runnerInfo.idom;
					}
				}
			}
		}
	}
}
