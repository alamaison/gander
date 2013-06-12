package uk.ac.ic.doc.gander.implementation;

import uk.ac.ic.doc.gander.Feature;

/**
 * Model of the concrete implementation of a value in a program.  
 */
public interface Implementation {
	
	/**
	 * Does the implementation define that values created from it support
	 * the given feature.
	 * 
	 * For example, if the implementation were a class, does the class define
	 * the objects created from it support a particular method feature.
	 * 
	 * @param feature  Feature whose support is in question.
	 */
	boolean definesSupportFor(Feature feature);
}
