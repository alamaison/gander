package uk.ac.ic.doc.gander.model;

import org.python.pydev.parser.jython.SimpleNode;

/**
 * Model element that can appear within a {@link OldNamespace} but that isn't
 * necessarily a {@link OldNamespace} itself.
 * 
 * For example, {@link OldNamespace}s could contain data members but these
 * data members can't contain others in the model.
 */
@Deprecated
public interface Member {

	public abstract String getName();

	public abstract OldNamespace getParentScope();

	public abstract SimpleNode getAst();

}