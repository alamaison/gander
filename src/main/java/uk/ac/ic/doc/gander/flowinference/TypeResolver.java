package uk.ac.ic.doc.gander.flowinference;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.model.ModelSite;

public class TypeResolver {

    private final TimingTypeEngine engine;

    public TypeResolver(TimingTypeEngine engine) {
        this.engine = engine;
    }

    public TypeResolver(TypeEngine engine) {
        this.engine = new TimingTypeEngine(engine);
    }

    public PyObject typeOf(ModelSite<exprType> expression) {

        Result<PyObject> types = engine.typeOf(expression);

        return types.transformResult(new Singletoniser());
    }

    private final class Singletoniser implements Transformer<PyObject, PyObject> {

        @Override
        public PyObject transformFiniteResult(java.util.Set<PyObject> result) {
            if (result.size() == 1) {
                return result.iterator().next();
            } else {
                System.err.println("Oh dear, not a singleton: " + result);
                return null;
            }
        }

        @Override
        public PyObject transformInfiniteResult() {
            System.err.println("Oh dear, Top");
            return null;
        }
    }

    public long flowCost() {
        return engine.milliseconds();
    }
}
