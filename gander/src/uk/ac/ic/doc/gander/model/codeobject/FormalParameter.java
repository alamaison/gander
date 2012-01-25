package uk.ac.ic.doc.gander.model.codeobject;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.model.ModelSite;

public interface FormalParameter {

	ModelSite<? extends exprType> parameterSite();

}