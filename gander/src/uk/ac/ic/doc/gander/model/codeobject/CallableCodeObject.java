package uk.ac.ic.doc.gander.model.codeobject;


/**
 * Code objects whose object can be subject to implicit and explicit calling.
 * 
 * All callable code object are also nested as their default arguments require a
 * parent code block to be resolved with respect to.
 */
public interface CallableCodeObject extends NestedCodeObject {

	/**
	 * Returns the parameters of the callable code object.
	 */
	FormalParameters formalParameters();
}
