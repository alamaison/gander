package uk.ac.ic.doc.gander.flowinference.typegoals.namespacename;

import java.util.Collections;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.List;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.Tuple;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ast.BindingDetector;
import uk.ac.ic.doc.gander.ast.LocalCodeBlockVisitor;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyClass;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyFunction;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.flowinference.typegoals.expression.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.typegoals.parameter.ParameterTypeGoal;
import uk.ac.ic.doc.gander.importing.ImportFactory;
import uk.ac.ic.doc.gander.importing.ImportStatement;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;
import uk.ac.ic.doc.gander.model.codeobject.FunctionCO;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

/**
 * Find conservative approximation of the types bound to a given name in a
 * particular code block.
 */
final class VariableTypeSummariser {

    private final RedundancyEliminator<PyObject> types = new RedundancyEliminator<PyObject>();

    VariableTypeSummariser(Variable variable, SubgoalManager manager) {

        types.add(new BoundTypeVisitor(manager, variable).getJudgement());
        if (types.isFinished()) {
            return;
        }

        /*
         * For names that bind in the global namespace, we must add any values
         * bound to the name in the builtin namespace as well. This is the
         * mythical 'top-level' namespace where the decision as to which one the
         * value comes from is made at runtime.
         */
        if (new NamespaceName(variable.bindingLocation()).namespace().equals(
                variable.codeObject().oldStyleConflatedNamespace()
                        .getGlobalNamespace())) {

            types.add(new BoundTypeVisitor(manager, new Variable(variable
                    .name(), variable.model().getTopLevel().codeObject()))
                    .getJudgement());
        }

    }

    Result<PyObject> solution() {
        return types.result();
    }

}

class BoundTypeVisitor implements BindingDetector.DetectionEvent {
    private final SubgoalManager goalManager;
    private final Variable variable;
    private final RedundancyEliminator<PyObject> judgement = new RedundancyEliminator<PyObject>();
    private final BindingDetector detector = new BindingDetector(this);

    BoundTypeVisitor(SubgoalManager goalManager, Variable variable) {
        this.goalManager = goalManager;
        this.variable = variable;

        processParameters(variable, goalManager);

        if (!judgement.isFinished()) {
            processBody(variable);
        }
    }

    private boolean isMatch(String name) {
        return name.equals(variable.name());
    }

    private void processBody(Variable variable) {
        class BodyProcessor extends LocalCodeBlockVisitor {

            @Override
            protected Object seenNestedClassDef(ClassDef node) throws Exception {
                return node.accept(detector);
            }

            @Override
            protected Object seenNestedFunctionDef(FunctionDef node)
                    throws Exception {
                return node.accept(detector);
            }

            @Override
            protected Object unhandled_node(SimpleNode node) throws Exception {
                return node.accept(detector);
            }

            @Override
            public void traverse(SimpleNode node) throws Exception {
                if (!judgement.isFinished()) {
                    /*
                     * Traverse by default so that we catch all assignments even
                     * if they are nested. The LocalCodeBlockVisitor takes care
                     * of stopping us traversing into class and function
                     * definitions.
                     */
                    node.traverse(this);
                }
            }
        }

        try {
            variable.codeObject().codeBlock().accept(new BodyProcessor());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void processParameters(Variable variable, SubgoalManager goalManager) {

        if (variable.codeObject() instanceof InvokableCodeObject) {

            InvokableCodeObject invokable = (InvokableCodeObject) variable
                    .codeObject();

            if (invokable.formalParameters().hasVariableBindingParameter(
                    variable)) {

                judgement.add(goalManager
                        .registerSubgoal(new ParameterTypeGoal(invokable,
                                variable)));
            }
        }
    }

    public Result<PyObject> getJudgement() {
        return judgement.result();
    }

    @Override
    public void assignment(exprType[] lhs, exprType rhs) {

        if (lhs.length == 1) {

            exprType target = lhs[0];

            if (target instanceof Name && isMatch(((Name) target).id)) {

                ModelSite<exprType> rhsSite = new ModelSite<exprType>(rhs,
                        variable.codeObject());
                Result<PyObject> rhsType = goalManager
                        .registerSubgoal(new ExpressionTypeGoal(rhsSite));

                judgement.add(rhsType);

            } else {
                unpackNestedTargetsAndTopifyIfMatch(lhs);
            }

        } else {
            unpackNestedTargetsAndTopifyIfMatch(lhs);
        }
    }

    private void unpackNestedTargetsAndTopifyIfMatch(exprType[] lhs) {
        for (exprType target : lhs) {
            unpackNestedTargetsAndTopifyIfMatch(target);
        }
    }

    /**
     * Digs into the target assigning Top to our judgement if our variable is
     * nested anywhere within in.
     */
    private void unpackNestedTargetsAndTopifyIfMatch(exprType target) {
        if (target instanceof Name && isMatch(((Name) target).id)) {
            judgement.add(TopT.INSTANCE);
        } else if (target instanceof Tuple) {
            unpackNestedTargetsAndTopifyIfMatch(((Tuple) target).elts);
        } else if (target instanceof List) {
            unpackNestedTargetsAndTopifyIfMatch(((List) target).elts);
        } else {
            // Nothing else can lead to a variable binding so we don't care
        }
    }

    @Override
    public void classDefiniton(String name, ClassDef node) {

        if (isMatch(name)) {
            ClassCO klass = (ClassCO) variable.codeObject().nestedCodeObjects()
                    .findCodeObjectMatchingAstNode(node);
            assert klass != null;
            judgement.add(new FiniteResult<PyObject>(Collections
                    .singleton(new PyClass(klass))));
        }

        /*
         * Do NOT recurse into the ClassDef body. Despite appearances, it is not
         * part of this namespace's code object. It is a declaration of the
         * nested class's code object. Another way to think about it: the
         * class's body is not being 'executed' now whereas the enclosing
         * namespace's body is.
         */
    }

    @Override
    public void functionDefinition(String name, FunctionDef node) {

        if (isMatch(name)) {
            FunctionCO function = (FunctionCO) variable.codeObject()
                    .nestedCodeObjects().findCodeObjectMatchingAstNode(node);
            assert function != null;
            judgement.add(new FiniteResult<PyObject>(Collections
                    .singleton(new PyFunction(function))));
        }

        /*
         * Do NOT recurse into the FunctionDef body. Despite appearances, it is
         * not part of this namespace's code object. It is a declaration of the
         * nested function's code object. Another way to think about it: the
         * nested function's body is not being 'executed' now whereas the
         * enclosing namespace's body is.
         */

    }

    @Override
    public void forLoop(exprType target, exprType iterable) {

        if (target instanceof Name) {
            if (isMatch(((Name) target).id)) {
                // TODO: Try to infer type of iterable
                judgement.add(TopT.INSTANCE);
            }
        } else if (target instanceof Tuple) {
            for (exprType tupleItem : ((Tuple) target).elts) {
                if (tupleItem instanceof Name && isMatch(((Name) tupleItem).id)) {
                    // TODO: Try to infer type of iterable
                    judgement.add(TopT.INSTANCE);
                }
            }
        } else {
            System.err.println("Funny for loop target: " + target);
        }
    }

    @Override
    public boolean importStatement(ImportStatement spec) {

        ModuleCO relativeTo;
        if (variable.codeObject().enclosingModule()
                .oldStyleConflatedNamespace().getParentScope() != null) {
            relativeTo = (ModuleCO) variable.codeObject().enclosingModule()
                    .oldStyleConflatedNamespace().getParentScope().codeObject();
        } else {
            relativeTo = null;
        }

        judgement.add(new ImportTypeMapper(goalManager)
                .typeImport(
                        variable,
                        ImportFactory.newImport(spec, relativeTo,
                                variable.codeObject())));

        return judgement.isFinished();
    }

    @Override
    public boolean exceptionHandler(exprType name, exprType type) {
        if (name instanceof Name) {
            if (isMatch(((Name) name).id)) {

                /*
                 * If any of the above attempts to convert the declared type to
                 * a model class fail, we must add TopT as we _have_ found the
                 * name, we just don't know its type. Not adding anything would
                 * mean we found no binding for that name which would be a lie.
                 */
                if (type instanceof Name) {

                    /*
                     * Exceptions are the one place that Python's type model is
                     * very un-pythonic.
                     * 
                     * Exceptions are caught by named type. Although it seems
                     * like we should be able to use that named type here, it
                     * doesn't fit well with the model we're using as the
                     * exception object isn't necessarily an instance of the
                     * named exception class. It could be an instance of a
                     * _subtype_.
                     * 
                     * For the moment we just return Top here until we think of
                     * a better way.
                     * 
                     * goalManager.registerSubgoal(new ExpressionTypeGoal(new
                     * ModelSite<exprType>(type, variable.codeObject())));
                     */
                    judgement.add(TopT.INSTANCE);

                } else {
                    // TODO: Try to resolve the expression to an
                    // exception class
                    judgement.add(TopT.INSTANCE);
                }
            }
        } else {
            // XXX: No idea what happens here. How could the
            // name of the exception object _not_ be a name?
        }
        return judgement.isFinished();
    }
}
