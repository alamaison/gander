package uk.ac.ic.doc.gander.interfacetype;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.model.ModelSite;

public interface InterfaceTypeSystem {

	/**
	 * Infer the interface type of the expression.
	 */
	public InterfaceType typeOf(ModelSite<? extends exprType> expression, BasicBlock containingBlock);
	
}
