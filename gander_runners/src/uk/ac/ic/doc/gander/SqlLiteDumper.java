package uk.ac.ic.doc.gander;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import uk.ac.ic.doc.gander.analysers.CallTargetTypeDiff.DiffResult;
import uk.ac.ic.doc.gander.analysers.CallTargetTypeDiff.ResultObserver;
import uk.ac.ic.doc.gander.calls.CallSite;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.result.Result.Processor;

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
		if (connection == null) {
			throw new RuntimeException("No connection");
		}

		try {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			readyCallsitesTable(statement);
			readyResultsTables(statement);
		} catch (SQLException e) {
			connection.close();
			throw e;
		}

	}

	public void resultReady(DiffResult result) {

		try {
			insertCallsite(result.callSite());
			insertResult(result);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	private void insertResult(final DiffResult result) throws SQLException {

		/* Duck types */
		result.duckType().actOnResult(new Processor<PyObject>() {

			public void processInfiniteResult() {

				try {
					PreparedStatement statement = connection
							.prepareStatement("insert into "
									+ duckResultsTableName
									+ " values(?, NULL);");
					statement.setString(1, result.callSite().toString());
					statement.execute();
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}

			public void processFiniteResult(Set<PyObject> flowTypes) {
				for (PyObject type : flowTypes) {

					try {
						insertDuckType(result, type);
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				}
			}
		});

		/* Flow types */
		result.flowType().actOnResult(new Processor<PyObject>() {

			public void processInfiniteResult() {

				try {
					PreparedStatement statement = connection
							.prepareStatement("insert into "
									+ flowResultsTableName
									+ " values(?, NULL);");
					statement.setString(1, result.callSite().toString());
					statement.execute();
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}

			public void processFiniteResult(Set<PyObject> flowTypes) {
				for (PyObject type : flowTypes) {

					try {
						insertFlowType(result, type);
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				}
			}
		});
	}

	private void insertDuckType(DiffResult result, PyObject type)
			throws SQLException {
		insertTypeInto(duckResultsTableName, result, type);
	}

	private void insertFlowType(DiffResult result, PyObject type)
			throws SQLException {
		insertTypeInto(flowResultsTableName, result, type);
	}

	private void insertTypeInto(String tableName, DiffResult result, PyObject type)
			throws SQLException {
		PreparedStatement statement = connection
				.prepareStatement("insert into " + tableName + " values(?, ?);");
		statement.setString(1, result.callSite().toString());
		statement.setString(2, type.toString());
		statement.execute();
	}

	private void insertCallsite(CallSite callSite) throws SQLException {
		assert connection != null;
		PreparedStatement statement = connection
				.prepareStatement("insert into " + callSitesTableName
						+ " values(?, ?, ?, ?);");
		statement.setString(1, callSite.toString());
		statement.setString(2, callSite.getCall().toString());
		statement.setString(3, callSite.getScope().toString());
		statement.setString(4, callSite.getBlock().toString());
		statement.execute();
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
				+ "call_site_name string NOT NULL, " + "type string)");
	}

	private void readyFlowResultsTable(Statement statement) throws SQLException {
		statement.executeUpdate("drop table if exists " + flowResultsTableName);
		statement.executeUpdate("create table " + flowResultsTableName + " ("
				+ "call_site_name string NOT NULL, " + "type string)");
	}
}
