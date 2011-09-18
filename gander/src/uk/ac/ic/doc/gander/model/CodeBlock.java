package uk.ac.ic.doc.gander.model;

import java.util.List;

import org.python.pydev.parser.jython.ast.VisitorIF;

public interface CodeBlock {

	/**
	 * Return the names that parameters are bound to locally.
	 * 
	 * Order is significant for deciding the types of the parameters so they are
	 * returned as a list in order.
	 * 
	 * XXX: This is a symptom of our conflated binding and type inference. When
	 * we separate these we can change this to a set which makes more sense
	 * (parameters can't occur twice with the same name - or can they).
	 */
	List<String> getFormalParameters();

	void accept(VisitorIF visitor) throws Exception;

}
