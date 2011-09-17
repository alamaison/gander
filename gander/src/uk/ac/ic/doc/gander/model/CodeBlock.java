package uk.ac.ic.doc.gander.model;

import java.util.Set;

import org.python.pydev.parser.jython.ast.VisitorIF;

public interface CodeBlock {

	Set<String> getFormalParameters();

	void accept(VisitorIF visitor) throws Exception;

}
