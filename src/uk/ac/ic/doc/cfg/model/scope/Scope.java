package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assert;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.AugAssign;
import org.python.pydev.parser.jython.ast.BoolOp;
import org.python.pydev.parser.jython.ast.Break;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.Compare;
import org.python.pydev.parser.jython.ast.Continue;
import org.python.pydev.parser.jython.ast.Expr;
import org.python.pydev.parser.jython.ast.For;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.If;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.Pass;
import org.python.pydev.parser.jython.ast.Raise;
import org.python.pydev.parser.jython.ast.Return;
import org.python.pydev.parser.jython.ast.Str;
import org.python.pydev.parser.jython.ast.Subscript;
import org.python.pydev.parser.jython.ast.TryExcept;
import org.python.pydev.parser.jython.ast.TryFinally;
import org.python.pydev.parser.jython.ast.UnaryOp;
import org.python.pydev.parser.jython.ast.VisitorBase;
import org.python.pydev.parser.jython.ast.While;
import org.python.pydev.parser.jython.ast.Yield;

import uk.ac.ic.doc.cfg.model.BasicBlock;
import uk.ac.ic.doc.cfg.model.scope.Statement.Exit;

public abstract class Scope extends VisitorBase {

	private Statement previousStatement;
	private boolean startInNewBlock;
	private Exit trajectory;

	// Internal members only used to passing arguments to accept()
	private Statement _previousStatement = null;
	private Exit _trajectory = null;
	private boolean _startInNewBlock = false;

	protected boolean getStartInNewBlock() {
		return startInNewBlock;
	}

	protected void setStartInNewBlock(boolean startInNewBlock) {
		this.startInNewBlock = startInNewBlock;
	}

	public Scope() {
		startInNewBlock = true;
		setPreviousStatement(new Statement());
		this.trajectory = previousStatement().fallthroughs();
	}

	public Scope(Statement previousStatement, Exit trajectory,
			boolean startInNewBlock) {
		this.trajectory = trajectory;
		this.startInNewBlock = startInNewBlock;
		setPreviousStatement(previousStatement);
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
	public Object visitWhile(While node) throws Exception {
		return delegateScope(new WhileScope(node, _previousStatement,
				_trajectory, _startInNewBlock, this));
	}

	@Override
	public Object visitAssign(Assign node) throws Exception {
		return delegateSelfAddingScope(node);
	}

	@Override
	public Object visitExpr(Expr node) throws Exception {
		return delegateScope(new ExprScope(node, _previousStatement,
				_trajectory, _startInNewBlock, this));
	}

	@Override
	public Object visitCall(Call node) throws Exception {
		return delegateSelfAddingScope(node);
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
	public Object visitCompare(Compare node) throws Exception {
		return delegateSelfAddingScope(node);
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
		// TODO: pull referenced variables out of node
		return delegateSelfAddingScope(node);
	}

	@Override
	public Object visitUnaryOp(UnaryOp node) throws Exception {
		// TODO: pull referenced variables out of node
		return delegateSelfAddingScope(node);
	}

	@Override
	public Object visitImportFrom(ImportFrom node) throws Exception {
		System.err.println("WARNING: nested __import__");
		return delegateSelfAddingScope(node);
	}

	@Override
	public Object visitFunctionDef(FunctionDef node) throws Exception {
		// We don't analyse flow in nested function definitions. Just
		// add AST as-is to control graph so we can detect presence of
		// token and treat like a local variable.
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
	public void traverse(SimpleNode node) throws Exception {
		node.traverse(this);
	}

	@Override
	protected Object unhandled_node(SimpleNode node) throws Exception {
		System.err.println("Unhandled node: " + node);
		Statement statement = new Statement();
		return statement;
	}

	protected Statement delegateScope(SimpleNode node, Statement previous,
			Statement.Exit trajectory, boolean startInNewBlock)
			throws Exception {
		if (node == null)
			return new Statement();

		assert _previousStatement == null;
		assert _trajectory == null;
		assert !_startInNewBlock;

		try {
			_previousStatement = previous;
			_trajectory = trajectory;
			_startInNewBlock = startInNewBlock;

			return (Statement) node.accept(this);
		} finally {
			_previousStatement = null;
			_trajectory = null;
			_startInNewBlock = false;
		}
	}

	protected Statement delegateScopeContinuing(SimpleNode node)
			throws Exception {
		return delegateScope(node, previousStatement(), trajectory(),
				getStartInNewBlock());
	}

	protected Statement delegateScopeForceNew(SimpleNode node) throws Exception {
		return delegateScope(node, previousStatement(), trajectory(), true);
	}

	protected Statement previousStatement() {
		return previousStatement;
	}

	protected void setPreviousStatement(Statement previousStatement) {
		this.previousStatement = previousStatement;
	}

	protected Exit trajectory() {
		return trajectory;
	}

	protected void setTrajectory(Exit trajectory) {
		this.trajectory = trajectory;
	}

	/**
	 * In the case where the previous statement's result has a single
	 * fallthrough block, this can be considered the current block as it is
	 * still eligible to have statements added to it.
	 * 
	 * @return Previous statement's unique fallthrough block.
	 */
	protected BasicBlock currentBlock() {
		return previousStatement().uniqueFallthrough();
	}

	protected abstract Statement process() throws Exception;

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

	protected void addToNewBlock(SimpleNode node) {
		BasicBlock nextBlock = newBlock();
		nextBlock.addStatement(node);

		Statement statement = new Statement();
		statement.inlink(nextBlock);
		statement.fallthrough(nextBlock);
		trajectory().linkTo(statement);

		setPreviousStatement(statement);
		setTrajectory(statement.fallthroughs());
	}
}