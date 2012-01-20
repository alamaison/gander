package uk.ac.ic.doc.gander.model;

import org.python.pydev.parser.jython.SimpleNode;

/**
 * Model element that can appear within a {@link Namespace} but that isn't
 * necessarily a {@link Namespace} itself.
 * 
 * For example, {@link Namespace}s could contain data members but these
 * data members can't contain others in the model.
 */
@Deprecated
public interface Member {

	public abstract String getName();

	public abstract Namespace getParentScope();

	public abstract SimpleNode getAst();

}