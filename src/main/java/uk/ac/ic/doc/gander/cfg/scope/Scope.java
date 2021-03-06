package uk.ac.ic.doc.gander.cfg.scope;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assert;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.AugAssign;
import org.python.pydev.parser.jython.ast.BinOp;
import org.python.pydev.parser.jython.ast.BoolOp;
import org.python.pydev.parser.jython.ast.Break;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.Compare;
import org.python.pydev.parser.jython.ast.Continue;
import org.python.pydev.parser.jython.ast.Delete;
import org.python.pydev.parser.jython.ast.Dict;
import org.python.pydev.parser.jython.ast.Exec;
import org.python.pydev.parser.jython.ast.Expr;
import org.python.pydev.parser.jython.ast.For;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Global;
import org.python.pydev.parser.jython.ast.If;
import org.python.pydev.parser.jython.ast.IfExp;
import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.Lambda;
import org.python.pydev.parser.jython.ast.List;
import org.python.pydev.parser.jython.ast.ListComp;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.Num;
import org.python.pydev.parser.jython.ast.Pass;
import org.python.pydev.parser.jython.ast.Print;
import org.python.pydev.parser.jython.ast.Raise;
import org.python.pydev.parser.jython.ast.Repr;
import org.python.pydev.parser.jython.ast.Return;
import org.python.pydev.parser.jython.ast.Str;
import org.python.pydev.parser.jython.ast.StrJoin;
import org.python.pydev.parser.jython.ast.Subscript;
import org.python.pydev.parser.jython.ast.TryExcept;
import org.python.pydev.parser.jython.ast.TryFinally;
import org.python.pydev.parser.jython.ast.Tuple;
import org.python.pydev.parser.jython.ast.UnaryOp;
import org.python.pydev.parser.jython.ast.VisitorBase;
import org.python.pydev.parser.jython.ast.While;
import org.python.pydev.parser.jython.ast.Yield;

import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.cfg.scope.Statement.Exit;

abstract class Scope extends VisitorBase {

    private Statement previousStatement;
    private boolean startInNewBlock;
    private Exit trajectory;

    // Internal members only used to passing arguments to accept()
    private Statement _previousStatement = null;
    private Exit _trajectory = null;
    private boolean _startInNewBlock = false;

    private boolean getStartInNewBlock() {
        return startInNewBlock;
    }

    public Scope() {
        startInNewBlock = true;
        this.previousStatement = new Statement();
        this.trajectory = previousStatement().fallthroughs();
    }

    protected Scope(Statement previousStatement, Exit trajectory,
            boolean startInNewBlock) {
        this.trajectory = trajectory;
        this.startInNewBlock = startInNewBlock;
        this.previousStatement = previousStatement;
    }

    private Statement delegateScope(Scope scope) throws Exception {
        return scope.process();
    }

    private <T extends SimpleNode> Statement delegateSelfAddingScope(T node)
            throws Exception {
        return delegateScope(new SelfAddingScope<T>(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitIf(If node) throws Exception {
        return delegateScope(new IfScope(node, _previousStatement, _trajectory,
                _startInNewBlock, this));
    }

    @Override
    public Object visitIfExp(IfExp node) throws Exception {
        return delegateScope(new IfExpScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitWhile(While node) throws Exception {
        return delegateScope(new WhileScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitAssign(Assign node) throws Exception {
        return delegateScope(new AssignScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitExpr(Expr node) throws Exception {
        return delegateScope(new ExprScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitCall(Call node) throws Exception {
        return delegateScope(new CallScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitBreak(Break node) throws Exception {
        return delegateScope(new BreakScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitContinue(Continue node) throws Exception {
        return delegateScope(new ContinueScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitReturn(Return node) throws Exception {
        return delegateScope(new ReturnScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitFor(For node) throws Exception {
        return delegateScope(new ForScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitYield(Yield node) throws Exception {
        return delegateScope(new YieldScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitAugAssign(AugAssign node) throws Exception {
        return delegateSelfAddingScope(node);
    }

    @Override
    public Object visitStr(Str node) throws Exception {
        return delegateSelfAddingScope(node);
    }

    @Override
    public Object visitStrJoin(StrJoin node) throws Exception {
        return delegateSelfAddingScope(node);
    }

    @Override
    public Object visitNum(Num node) throws Exception {
        return delegateSelfAddingScope(node);
    }

    // Python backticks: `object` equivalent to repr(object)
    @Override
    public Object visitRepr(Repr node) throws Exception {
        return delegateSelfAddingScope(node);
    }

    @Override
    public Object visitCompare(Compare node) throws Exception {
        return delegateScope(new CompareScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitName(Name node) throws Exception {
        return delegateSelfAddingScope(node);
    }

    @Override
    public Object visitAttribute(Attribute node) throws Exception {
        return delegateSelfAddingScope(node);
    }

    @Override
    public Object visitSubscript(Subscript node) throws Exception {
        return delegateSelfAddingScope(node);
    }

    @Override
    public Object visitBoolOp(BoolOp node) throws Exception {
        return delegateScope(new BoolOpScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitUnaryOp(UnaryOp node) throws Exception {
        return delegateScope(new UnaryOpScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitImportFrom(ImportFrom node) throws Exception {
        // TODO: actually handle imports
        System.err.println("WARNING: nested __import__");
        return delegateScope(new PassScope(_previousStatement, _trajectory,
                _startInNewBlock, this));
    }

    @Override
    public Object visitFunctionDef(FunctionDef node) throws Exception {
        // We don't analyse flow in nested function definitions. Just
        // add AST as-is to control graph so we can detect presence of
        // callable object and treat like a local variable.
        return delegateSelfAddingScope(node);
    }

    @Override
    public Object visitLambda(Lambda node) throws Exception {
        // We don't analyse flow in the body of lambdas. Just
        // add AST as-is to control graph so we can detect presence of
        // callable object and treat like a local variable.
        return delegateSelfAddingScope(node);
    }

    @Override
    public Object visitClassDef(ClassDef node) throws Exception {
        // We don't analyse flow in nested class definitions. Just
        // add AST as-is to control graph so we can detect presence of
        // token and treat like a local variable.
        return delegateSelfAddingScope(node);
    }

    @Override
    public Object visitDelete(Delete node) throws Exception {
        // We add the whole delete node to the graph so that later analyses
        // can recognise the removal of a name if they need to do data-flow
        // analysis
        return delegateSelfAddingScope(node);
    }

    @Override
    public Object visitRaise(Raise node) throws Exception {
        return delegateScope(new RaiseScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitPass(Pass node) throws Exception {
        return delegateScope(new PassScope(_previousStatement, _trajectory,
                _startInNewBlock, this));
    }

    @Override
    public Object visitTryExcept(TryExcept node) throws Exception {
        return delegateScope(new TryExceptScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitTryFinally(TryFinally node) throws Exception {
        return delegateScope(new TryFinallyScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitAssert(Assert node) throws Exception {
        return delegateScope(new PassScope(_previousStatement, _trajectory,
                _startInNewBlock, this));
    }

    @Override
    public Object visitImport(Import node) throws Exception {
        // XXX: can importing throw an exception?  Do we care?
        return delegateSelfAddingScope(node);
    }

    @Override
    public Object visitPrint(Print node) throws Exception {
        return delegateScope(new PrintScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitBinOp(BinOp node) throws Exception {
        return delegateScope(new BinOpScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitList(List node) throws Exception {
        return delegateScope(new ListScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitDict(Dict node) throws Exception {
        return delegateScope(new DictScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitListComp(ListComp node) throws Exception {
        return delegateScope(new ListCompScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitTuple(Tuple node) throws Exception {
        return delegateScope(new TupleScope(node, _previousStatement,
                _trajectory, _startInNewBlock, this));
    }

    @Override
    public Object visitGlobal(Global node) throws Exception {
        return delegateScope(new PassScope(_previousStatement, _trajectory,
                _startInNewBlock, this));
    }

    @Override
    public Object visitExec(Exec node) throws Exception {
        return delegateScope(new PassScope(_previousStatement, _trajectory,
                _startInNewBlock, this));
    }

    @Override
    public void traverse(SimpleNode node) throws Exception {
        node.traverse(this);
    }

    @Override
    protected Object unhandled_node(SimpleNode node) throws Exception {
        System.err.println("Unhandled node: " + node);
        Statement statement = new Statement();
        return statement;
    }

    private Statement buildGraph(SimpleNode node, Statement previous,
            Exit trajectory, boolean startInNewBlock) {
        if (node == null) {
            Statement statement = new Statement();
            statement.inlinks().inherit(trajectory());
            statement.fallthroughs().inherit(trajectory());
            return statement;
        }

        assert _previousStatement == null;
        assert _trajectory == null;
        assert !_startInNewBlock;

        try {
            _previousStatement = previous;
            _trajectory = trajectory;
            _startInNewBlock = startInNewBlock;

            return (Statement) node.accept(this);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failure while generating control-flow graph", e);
        } finally {
            _previousStatement = null;
            _trajectory = null;
            _startInNewBlock = false;
        }
    }

    private Statement buildGraph(SimpleNode[] nodes, Statement previous,
            Exit trajectory, boolean startInNewBlock) {
        return new BodyScope(nodes, previous, trajectory, startInNewBlock, this)
                .process();
    }

    protected Statement delegate(SimpleNode node) {
        return buildGraph(node, previousStatement(), trajectory(),
                getStartInNewBlock());
    }

    protected Statement delegate(SimpleNode[] nodes) {
        return buildGraph(nodes, previousStatement(), trajectory(),
                getStartInNewBlock());
    }

    protected Statement buildGraph(SimpleNode node, Statement previous,
            Exit trajectory) {
        return buildGraph(node, previous, trajectory, false);
    }

    protected Statement buildGraph(SimpleNode[] nodes, Statement previous,
            Exit trajectory) {
        return buildGraph(nodes, previous, trajectory, false);
    }

    protected Statement delegateButForceNewBlock(SimpleNode node){
        return buildGraph(node, previousStatement(), trajectory(), true);
    }

    protected Statement delegateButForceNewBlock(SimpleNode[] nodes) {
        return buildGraph(nodes, previousStatement(), trajectory(), true);
    }

    protected Statement buildGraphForceNewBlock(SimpleNode node,
            Statement previous, Exit trajectory){
        return buildGraph(node, previous, trajectory, true);
    }

    protected Statement buildGraphForceNewBlock(SimpleNode[] nodes,
            Statement previous, Exit trajectory) {
        return buildGraph(nodes, previous, trajectory, true);
    }

    protected Statement previousStatement() {
        return previousStatement;
    }

    protected Exit trajectory() {
        return trajectory;
    }

    /**
     * In the case where the previous statement's result has a single
     * fallthrough block, this can be considered the current block as it is
     * still eligible to have statements added to it.
     * 
     * @return Previous statement's unique fallthrough block.
     */
    private BasicBlock currentBlock() {
        return previousStatement().uniqueFallthrough();
    }

    protected abstract Statement process();

    protected abstract BasicBlock newBlock();

    protected void addToCurrentBlock(SimpleNode node) {
        // XXX: The previousStatement is not yet fully linked so doesn't know
        // if it really is the end. How do we deal with this?
        if (!startInNewBlock && !previousStatement().isEndOfBlock()) {
            currentBlock().addStatement(node);
        } else {
            addToNewBlock(node);
        }
    }

    private void addToNewBlock(SimpleNode node) {
        BasicBlock nextBlock = newBlock();
        nextBlock.addStatement(node);

        Statement statement = new Statement();
        statement.inlink(nextBlock);
        statement.fallthrough(nextBlock);
        trajectory().linkTo(statement);

        this.previousStatement = statement;
        this.trajectory = statement.fallthroughs();
        this.startInNewBlock = false;
    }
}