package uk.ac.ic.doc.gander;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import uk.ac.ic.doc.gander.analysers.CallTargetTypeDiff.DiffResult;
import uk.ac.ic.doc.gander.analysers.CallTargetTypeDiff.ResultObserver;
import uk.ac.ic.doc.gander.calls.CallSite;
import uk.ac.ic.doc.gander.flowinference.result.Result.Processor;
import uk.ac.ic.doc.gander.flowinference.types.Type;

final class SqlLiteDumper implements ResultObserver {

	private final Connection connection;
	private static final String callSitesTableName = "callsites";
	private static final String duckResultsTableName = "duckresults";
	private static final String flowResultsTableName = "flowresults";

	public SqlLiteDumper() throws ClassNotFoundException, SQLException {
		/*
		 * load the sqlite-JDBC driver using the current class loader
		 */
		Class.forName("org.sqlite.JDBC");

		connection = DriverManager.getConnection("jdbc:sqlite:diffresults.db");

		try {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			readyCallsitesTable(statement);
			readyResultsTables(statement);
		} catch (SQLException e) {
			try {
				connection.close();
			} catch (SQLException e2) {
				// connection close failed.
				System.err.println(e2);
			}
			throw e;
		}

	}

	public void resultReady(DiffResult result) {

		try {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			insertCallsite(result.callSite(), statement);
			insertResult(result, statement);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	private void insertResult(final DiffResult result, final Statement statement)
			throws SQLException {

		/* Duck types */
		for (Type type : result.duckType()) {
			insertDuckType(result, statement, type);
		}

		/* Flow types */
		result.flowType().actOnResult(new Processor<Type>() {

			public void processInfiniteResult() {

				try {
					statement.executeUpdate("insert into "
							+ flowResultsTableName + " values("
							+ escape(result.callSite()) + ", NULL)");
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}

			public void processFiniteResult(Set<Type> flowTypes) {
				for (Type type : flowTypes) {

					try {
						insertFlowType(result, statement, type);
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				}
			}
		});
	}

	private void insertDuckType(DiffResult result, Statement statement,
			Type type) throws SQLException {
		insertTypeInto(duckResultsTableName, result, statement, type);
	}

	private void insertFlowType(DiffResult result, Statement statement,
			Type type) throws SQLException {
		insertTypeInto(flowResultsTableName, result, statement, type);
	}

	private void insertTypeInto(String tableName, DiffResult result,
			Statement statement, Type type) throws SQLException {
		statement.executeUpdate("insert into " + tableName + " values("
				+ escape(result.callSite()) + ", " + escape(type) + ")");
	}

	private void insertCallsite(CallSite callSite, Statement statement)
			throws SQLException {
		statement.executeUpdate("insert into " + callSitesTableName
				+ " values(" + escape(callSite) + ", "
				+ escape(callSite.getCall()) + ", "
				+ escape(callSite.getScope()) + ", "
				+ escape(callSite.getBlock()) + ")");
	}

	private <T> String escape(T string) {
		return "'" + string + "'";
	}

	private void readyCallsitesTable(Statement statement) throws SQLException {
		statement.executeUpdate("drop table if exists " + callSitesTableName);
		statement.executeUpdate("create table " + callSitesTableName + " ("
				+ "name string NOT NULL PRIMARY KEY, "
				+ "call string NOT NULL, " + "code_object string NOT NULL, "
				+ "basic_block string NOT NULL)");
	}

	private void readyResultsTables(Statement statement) throws SQLException {
		readyDuckResultsTable(statement);
		readyFlowResultsTable(statement);
	}

	private void readyDuckResultsTable(Statement statement) throws SQLException {
		statement.executeUpdate("drop table if exists " + duckResultsTableName);
		statement.executeUpdate("create table " + duckResultsTableName + " ("
				+ "call_site_name string NOT NULL, " + "type string NOT NULL)");
	}

	private void readyFlowResultsTable(Statement statement) throws SQLException {
		statement.executeUpdate("drop table if exists " + flowResultsTableName);
		statement.executeUpdate("create table " + flowResultsTableName + " ("
				+ "call_site_name string NOT NULL, " + "type string)");
	}
}
