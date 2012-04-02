package uk.ac.ic.doc.gander.analysers;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ParseException;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.analysis.CallFinder;
import uk.ac.ic.doc.gander.analysis.CallFinder.EventHandler;
import uk.ac.ic.doc.gander.calls.CallSite;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.duckinference.DuckTyper;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.ZeroCfaTypeEngine;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyWalker;
import uk.ac.ic.doc.gander.hierarchy.Package;
import uk.ac.ic.doc.gander.hierarchy.SourceFile;
import uk.ac.ic.doc.gander.model.DefaultModel;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.ModelWalkerWithParent;
import uk.ac.ic.doc.gander.model.MutableModel;

public class CallTargetTypeDiff {

	private final MutableModel model;
	private final TypeResolver typer;
	private final Set<DiffResult> duckTypes = new HashSet<DiffResult>();
	private final File projectRoot;
	private final Hierarchy hierarchy;
	private final ZeroCfaTypeEngine engine;
	private final Set<ResultObserver> observers = new HashSet<ResultObserver>();

	public final class DiffResult {
		private final CallSite callsite;
		private final Result<Type> duckType;
		private final Result<Type> flowType;

		private DiffResult(CallSite callsite, Result<Type> duckType,
				Result<Type> flowType) {
			this.callsite = callsite;
			// XXX: HACK
			this.duckType = duckType;
			this.flowType = flowType;
		}

		public CallSite callSite() {
			return callsite;
		}

		public Result<Type> duckType() {
			return duckType;
		}

		public Result<Type> flowType() {
			return flowType;
		}

		public boolean resultsMatch() {
			return flowType.transformResult(new Transformer<Type, Boolean>() {

				@Override
				public Boolean transformFiniteResult(Set<Type> result) {
					return result.equals(duckType);
				}

				@Override
				public Boolean transformInfiniteResult() {
					return Boolean.FALSE;
				}
			});
		}

		public boolean resultsAreDisjoint() {
			return flowType.transformResult(new Transformer<Type, Boolean>() {

				@Override
				public Boolean transformFiniteResult(final Set<Type> flowResult) {
					return duckType
							.transformResult(new Transformer<Type, Boolean>() {

								@Override
								public Boolean transformFiniteResult(
										Set<Type> duckResult) {
									Set<Type> intersection = new HashSet<Type>(
											flowResult);
									intersection.retainAll(duckResult);
									return intersection.isEmpty();
								}

								@Override
								public Boolean transformInfiniteResult() {
									return false;
								}
							});
				}

				@Override
				public Boolean transformInfiniteResult() {
					return false;
				}
			});
		}
	}

	public interface ResultObserver {
		void resultReady(DiffResult result);
	}

	public CallTargetTypeDiff(Hierarchy hierarchy, File projectRoot,
			ResultObserver... observers) throws Exception {
		this.projectRoot = projectRoot;

		this.hierarchy = hierarchy;
		for (ResultObserver observer : observers) {
			this.observers.add(observer);
		}

		this.observers.add(new ResultObserver() {

			@Override
			public void resultReady(DiffResult result) {

				duckTypes.add(result);
			}
		});

		System.out.println("Creating model from hierarchy");
		this.model = new DefaultModel(hierarchy);
		System.out.println("Loading all non-system modules in hierarchy");
		new HierarchyLoader().walk(hierarchy);
		System.out.println("Performing flow-based type inference");
		engine = new ZeroCfaTypeEngine();
		this.typer = new TypeResolver(engine);
		System.out.println("Running signature analysis");
		new ModelDucker().walk(model);
	}

	public Set<DiffResult> result() {
		return duckTypes;
	}

	private final class ModelDucker extends ModelWalkerWithParent {

		@Override
		protected void visitFunction(Function function) {
			// only analyse methods within our target project's namespace
			if (function.isSystem())
				return;

			for (BasicBlock block : function.getCfg().getBlocks()) {

				for (Call call : calls(block)) {

					CallSite callsite = new CallSite(call, function, block);

					Result<Type> duckType = new DuckTyper(model, typer).typeOf(
							call, block, function);
					Result<Type> flowType = engine.typeOf(call.func,
							function.codeObject());

					informObservers(new DiffResult(callsite, duckType, flowType));
				}
			}
		}

		private Set<Call> calls(BasicBlock block) {

			final Set<Call> calls = new HashSet<Call>();

			new CallFinder(block, new EventHandler() {

				@Override
				public void foundCall(Call call) {
					calls.add(call);
				}
			});

			return calls;
		}

		private Set<Call> attributeCalls(BasicBlock block) {

			final Set<Call> calls = new HashSet<Call>();

			new CallFinder(block, new EventHandler() {

				@Override
				public void foundCall(Call call) {
					if (call.func instanceof Attribute) {
						calls.add(call);
					}
				}
			});

			return calls;
		}

		private void informObservers(DiffResult diffResult) {
			for (ResultObserver observer : observers) {
				observer.resultReady(diffResult);
			}
		}

		private File relativeModulePath() {
			return new File(projectRoot.toURI()
					.relativize(modulePath().toURI()).getPath());
		}

		private File modulePath() {

			SourceFile module = hierarchy.findSourceFile(getEnclosingModule()
					.getFullName());
			if (module != null) {
				return module.getFile();
			} else {
				Package pkg = hierarchy.findPackage(getEnclosingModule()
						.getFullName());
				if (pkg != null) {
					return pkg.getInitFile();
				}
			}

			return null;
		}

		private org.python.pydev.parser.jython.ast.Module getAst() {
			return getEnclosingModule().getAst();
		}
	}

	/**
	 * Load every module and package module in the hierarchy.
	 */
	private final class HierarchyLoader extends HierarchyWalker {

		@Override
		protected void visitSourceFile(SourceFile module) {
			try {
				if (!module.isSystem())
					model.loadModule(module.getFullyQualifiedName());
			} catch (ParseException e) {
				System.err.println("MISSED DATA WARNING: error while "
						+ "parsing module" + module.getFullyQualifiedName());
				System.err.println(e);
				return;
			} catch (IOException e) {
				System.err.println("MISSED DATA WARNING: error while "
						+ "finding module" + module.getFullyQualifiedName());
				System.err.println(e);
			}
		}

		@Override
		protected void visitPackage(Package pkg) {
			try {
				if (!pkg.isSystem())
					model.loadPackage(pkg.getFullyQualifiedName());
			} catch (ParseException e) {
				System.err.println("MISSED DATA WARNING: error while "
						+ "parsing package" + pkg.getFullyQualifiedName());
				System.err.println(e);
				return;
			} catch (IOException e) {
				System.err.println("MISSED DATA WARNING: error while "
						+ "finding package" + pkg.getFullyQualifiedName());
				System.err.println(e);
				return;
			}
		}
	}
}
