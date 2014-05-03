package uk.ac.ic.doc.gander.model.codeobject;

import uk.ac.ic.doc.gander.model.parameters.FormalParameters;

/**
 * Code objects whose code block can be invoked by call.
 * 
 * All code objects are, by definition, executable. This interface indicated
 * something more than that: that the body can be executed on demand, rather
 * than just as part of an elaboration phase, and optionally passed arguments to
 * its formal parameters.
 * 
 * Note that this is quite different from being callable. For instance, a class
 * code object is callable in that putting {@code ()} after a class code object
 * is not an error; it invokes the class's constructor. However, the class code
 * object's code block (aka body) is not executed as a result of this call.
 * 
 * All executable code object are nested as their default arguments require a
 * parent code block to be resolved with respect to.
 */
public interface InvokableCodeObject extends NestedCodeObject {

    /**
     * Returns the parameters of the callable code object.
     */
    FormalParameters formalParameters();
}
