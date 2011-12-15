package uk.ac.ic.doc.gander.model;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ast.AstParentNodeFinder;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

/**
 * Search that finds the parent of a given model site.
 */
public final class ParentSiteFinder {

	/**
	 * Returns the parent of a given site.
	 * 
	 * @param subject
	 *            site whose parent we are looking for
	 * @throws RuntimeException
	 *             if the subject node is not within the search tree
	 */
	public static ModelSite<SimpleNode> findParent(
			ModelSite<? extends exprType> subject) {
		CodeObject commonCodeObject = subject.codeObject();
		SimpleNode parent = AstParentNodeFinder.findParent(subject.astNode(),
				commonCodeObject.ast());
		return new ModelSite<SimpleNode>(parent, commonCodeObject);
	}

}
