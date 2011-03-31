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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import uk.ac.ic.doc.gander.cfg.model.BasicBlock;

/**
 * This class computes dominator and post-dominator information using the
 * Lengauer-Tarjan method.
 *
 * See A Fast Algorithm for Finding Dominators in a Flowgraph
 * T. Lengauer & R. Tarjan, ACM TOPLAS July 1979, pgs 121-141.
 *
 * This implementation runs in time O(n log n).  The time bound
 * could be changed to O(n * ack(n)) with a small change to the link and eval,
 * and an addition of a child field to the DFS info. In reality, the constant
 * overheads are high enough that the current method is faster in all but the
 * strangest artificially constructed examples.
 *
 * The basic idea behind this algorithm is to perform a DFS walk, keeping track
 * of various info about parents.  We then use this info to calculate the
 * dominators, using union-find structures to link together the DFS info,
 * then finally evaluate the union-find results to get the dominators.
 * This implementation is m log n because it does not perform union by
 * rank to keep the union-find tree balanced.
 */
public final class Dominators {
    /* postdom is true if we want post dominators */
    private final boolean postdom;

    /* {@code non-null;} method being processed */
    private final DomMethod meth;

    /** indexed by basic block index */
    private final Map<BasicBlock, DFSInfo> info;

    private final ArrayList<BasicBlock> vertex;

    /** {@code non-null;} the raw dominator info */
    private final Map<BasicBlock, DomFront.DomInfo> domInfos;

    /**
     * Constructs an instance.
     *
     * @param meth {@code non-null;} method to process
     * @param domInfos {@code non-null;} the raw dominator info
     * @param postdom true for postdom information, false for normal dom info
     */
    private Dominators(DomMethod meth, Map<BasicBlock, DomFront.DomInfo> domInfos,
            boolean postdom) {
        this.meth = meth;
        this.domInfos = domInfos;
        this.postdom = postdom;
        this.info = new HashMap<BasicBlock, DFSInfo>();
        this.vertex = new ArrayList<BasicBlock>();
    }

    /**
     * Constructs a fully-initialized instance. (This method exists so as
     * to avoid calling a large amount of code in the constructor.)
     *
     * @param meth {@code non-null;} method to process
     * @param domInfos {@code non-null;} the raw dominator info
     * @param postdom true for postdom information, false for normal dom info
     */
    public static Dominators make(DomMethod meth, Map<BasicBlock, DomFront.DomInfo>  domInfos,
            boolean postdom) {
        Dominators result = new Dominators(meth, domInfos, postdom);

        result.run();
        return result;
    }

    private Iterable<BasicBlock> getPreds(BasicBlock block) {
        if (postdom) {
            return block.getSuccessors();
        } else {
            return block.getPredecessors();
        }
    }

    /**
     * Performs path compress on the DFS info.
     *
     * @param in Basic block whose DFS info we are path compressing.
     */
    private void compress(BasicBlock in) {
        DFSInfo bbInfo = info.get(in);
        DFSInfo ancestorbbInfo = info.get(bbInfo.ancestor);

        if (ancestorbbInfo.ancestor != null) {
            ArrayList<BasicBlock> worklist = new ArrayList<BasicBlock>();
            HashSet<BasicBlock> visited = new HashSet<BasicBlock>();
            worklist.add(in);

            while (!worklist.isEmpty()) {
                int wsize = worklist.size();
                BasicBlock v = worklist.get(wsize - 1);
                DFSInfo vbbInfo = info.get(v);
                BasicBlock vAncestor = vbbInfo.ancestor;
                DFSInfo vabbInfo = info.get(vAncestor);

                // Make sure we process our ancestor before ourselves.
                if (visited.add(vAncestor) && vabbInfo.ancestor != null) {
                    worklist.add(vAncestor);
                    continue;
                }
                worklist.remove(wsize - 1);

                // Update based on ancestor info.
                if (vabbInfo.ancestor == null) {
                    continue;
                }
                BasicBlock vAncestorRep = vabbInfo.rep;
                BasicBlock vRep = vbbInfo.rep;
                if (info.get(vAncestorRep).semidom
                        < info.get(vRep).semidom) {
                    vbbInfo.rep = vAncestorRep;
                }
                vbbInfo.ancestor = vabbInfo.ancestor;
            }
        }
    }

    private BasicBlock eval(BasicBlock v) {
        DFSInfo bbInfo = info.get(v);

        if (bbInfo.ancestor == null) {
            return v;
        }

        compress(v);
        return bbInfo.rep;
    }

    /**
     * Performs dominator/post-dominator calculation for the control
     * flow graph.
     *
     * @param meth {@code non-null;} method to analyze
     */
    private void run() {
        BasicBlock root = postdom
                ? meth.getExitBlock() : meth.getEntryBlock();

        if (root != null) {
            vertex.add(root);
            domInfos.get(root).idom = root;
        }

        /*
         * First we perform a DFS numbering of the blocks, by
         * numbering the dfs tree roots.
         */

        DfsWalker walker = new DfsWalker();
        meth.forEachBlockDepthFirst(postdom, walker);

        // the largest semidom number assigned
        int dfsMax = vertex.size() - 1;

        // Now calculate semidominators.
        for (int i = dfsMax; i >= 2; --i) {
            BasicBlock w = vertex.get(i);
            DFSInfo wInfo = info.get(w);

            for (BasicBlock predBlock : getPreds(w)) {
                DFSInfo predInfo = info.get(predBlock);

                /*
                 * PredInfo may not exist in case the predecessor is
                 * not reachable.
                 */
                if (predInfo != null) {
                    int predSemidom = info.get(eval(predBlock)).semidom;
                    if (predSemidom < wInfo.semidom) {
                        wInfo.semidom = predSemidom;
                    }
                }
            }
            info.get(vertex.get(wInfo.semidom)).bucket.add(w);

            /*
             * Normally we would call link here, but in our O(m log n)
             * implementation this is equivalent to the following
             * single line.
             */
            wInfo.ancestor = wInfo.parent;

            // Implicity define idom for each vertex.
            ArrayList<BasicBlock> wParentBucket;
            wParentBucket = info.get(wInfo.parent).bucket;

            while (!wParentBucket.isEmpty()) {
                int lastItem = wParentBucket.size() - 1;
                BasicBlock last = wParentBucket.remove(lastItem);
                BasicBlock U = eval(last);
                if (info.get(U).semidom
                        < info.get(last).semidom) {
                    domInfos.get(last).idom = U;
                } else {
                    domInfos.get(last).idom = wInfo.parent;
                }
            }
        }

        // Now explicitly define the immediate dominator of each vertex
        for (int i =  2; i <= dfsMax; ++i) {
            BasicBlock w = vertex.get(i);
            if (domInfos.get(w).idom
                    != vertex.get(info.get(w).semidom)) {
                domInfos.get(w).idom
                        = domInfos.get(domInfos.get(w).idom).idom;
            }
        }
    }

    /**
     * Callback for depth-first walk through control flow graph (either
     * from the entry block or the exit block). Records the traversal order
     * in the {@code info}list.
     */
    private class DfsWalker implements BasicBlock.Visitor {
        private int dfsNum = 0;

        public void visitBlock(BasicBlock v, BasicBlock parent) {
            DFSInfo bbInfo = new DFSInfo();
            bbInfo.semidom = ++dfsNum;
            bbInfo.rep = v;
            bbInfo.parent = parent;
            vertex.add(v);
            info.put(v, bbInfo);
        }
    }

    private static final class DFSInfo {
        public int semidom;
        public BasicBlock parent;

        /**
         * rep(resentative) is known as "label" in the paper. It is the node
         * that our block's DFS info has been unioned to.
         */
        public BasicBlock rep;

        public BasicBlock ancestor;
        public ArrayList<BasicBlock> bucket;

        public DFSInfo() {
            bucket = new ArrayList<BasicBlock>();
        }
    }
}
