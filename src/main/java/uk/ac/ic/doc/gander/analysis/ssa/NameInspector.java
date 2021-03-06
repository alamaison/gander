package uk.ac.ic.doc.gander.analysis.ssa;

import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.expr_contextType;

import uk.ac.ic.doc.gander.cfg.BasicBlock;

public abstract class NameInspector {

    public Object inspect(BasicBlock block) {

        NameExtractor names = new NameExtractor(block);

        for (Name name : names.operations()) {

            switch (name.ctx) {
            case expr_contextType.Load:
                seenLoad(name);
                break;

            case expr_contextType.Store:
                seenStore(name);
                break;

            case expr_contextType.AugStore:
                seenAugStore(name);
                break;

            case expr_contextType.AugLoad:
                seenAugLoad(name);
                break;

            case expr_contextType.Del:
                seenDel(name);
                break;

            case expr_contextType.Param:
                seenParam(name);
                break;

            case expr_contextType.Artificial:
                seenArtificial(name);
                break;

            case expr_contextType.KwOnlyParam:
                seenKwOnlyParam(name);
                break;

            default:
                throw new Error("Unreachable name-use context: " + name.ctx);
            }
        }

        return null;
    }

    protected void seenKwOnlyParam(Name name) {
        unhandledName(name);
    }

    protected void seenArtificial(Name name) {
        unhandledName(name);
    }

    protected void seenParam(Name name) {
        unhandledName(name);
    }

    protected void seenDel(Name name) {
        unhandledName(name);
    }

    protected void seenAugLoad(Name name) {
        unhandledName(name);
    }

    protected void seenAugStore(Name name) {
        unhandledName(name);
    }

    protected void seenStore(Name name) {
        unhandledName(name);
    }

    protected void seenLoad(Name name) {
        unhandledName(name);
    }

    protected void unhandledName(Name name) {
        System.err.println("WARNING unhandled name-use context: "
                + expr_contextType.expr_contextTypeNames[name.ctx]);
    }
}
