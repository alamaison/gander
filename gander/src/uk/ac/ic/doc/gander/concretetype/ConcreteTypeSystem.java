package uk.ac.ic.doc.gander.concretetype;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.model.ModelSite;

public interface ConcreteTypeSystem {

	public ConcreteType typeOf(ModelSite<? extends exprType> expression,
			BasicBlock containingBlock);
}
