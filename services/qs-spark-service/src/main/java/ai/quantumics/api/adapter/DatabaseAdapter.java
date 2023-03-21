package ai.quantumics.api.adapter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import ai.quantumics.api.constants.QsConstants;
import ai.quantumics.api.req.ConnectorProperties;
import ai.quantumics.api.req.DataBaseRequest;
import ai.quantumics.api.util.ResultSetToJsonMapper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class DatabaseAdapter {

	private static final Gson gson = new Gson();

	public static JSONArray getDatabaseObjects(ConnectorProperties properties, String connectorType, String query,
			int records) throws SQLException {
		JSONArray objectList = new JSONArray();
		try (Connection con = getConnectionObject(properties, connectorType)) { // Need to do using java8
			PreparedStatement preparedStatement = con.prepareStatement(query);
			preparedStatement.setFetchSize(records);
			preparedStatement.setMaxRows(records);
			preparedStatement.setFetchDirection(ResultSet.FETCH_FORWARD);
			objectList = ResultSetToJsonMapper.getJSONDataFromResultSet(preparedStatement.executeQuery());

		} catch (final Exception e) {
			log.error("Exception occurred while executing getDatabaseObjects method {}", e.getMessage());
		}
		return objectList;
	}

	public static JSONArray getDatabaseObjects(PreparedStatement preparedStatement) throws SQLException {
		JSONArray objectList = new JSONArray();
		try {
			objectList = ResultSetToJsonMapper.getJSONDataFromResultSet(preparedStatement.executeQuery());

		} catch (final Exception e) {
			log.error("Exception occurred while executing getDatabaseObjects method {}", e.getMessage());
		}
		return objectList;
	}
	
	public static JSONArray getDatabaseMetadata(PreparedStatement preparedStatement) throws SQLException {
		JSONArray objectList = new JSONArray();
		try {
			objectList = ResultSetToJsonMapper.getJSONMetadaDataFromResultSet( preparedStatement.executeQuery());

		} catch (final Exception e) {
			log.error("Exception occurred while executing getDatabaseObjects method {}", e.getMessage());
		}
		return objectList;
	}

	public static Connection getConnectionObject(ConnectorProperties properties, String connectionType) {
		Connection con = null;
		ConnectorsFactory factory = new ConnectorsFactory();
		try {
			con = factory.getConnector(connectionType).getRDBMSConnection(properties).getConnection();
		} catch (Exception e) {
			log.error("Exception occurred while executing getConnectionObject method {}", e.getMessage());
		}
		return con;
	}

//	public static String getSqlQuery(String connectorType, String dbObject, String schemaName, String tableName) {
//		try {
//			if ("pgsql".equalsIgnoreCase(connectorType)) {
//				if (QsConstants.SCHEMA.equalsIgnoreCase(dbObject)) {
//					return "SELECT distinct table_schema  as name FROM information_schema.tables a join information_schema.schemata b\r\n"
//							+ "on a.table_schema = b.schema_name WHERE table_type = 'BASE TABLE' and schema_owner !='rdsadmin' ORDER BY table_schema";
//				} else if (QsConstants.TABLE.equalsIgnoreCase(dbObject) && !schemaName.isEmpty()) {
//					return "SELECT table_name as name FROM information_schema.tables a join information_schema.schemata b\r\n"
//							+ "on a.table_schema = b.schema_name WHERE table_type = 'BASE TABLE' and schema_owner !='rdsadmin'\r\n"
//							+ "and table_schema='" + schemaName + "' ORDER BY table_name";
//				} else if (QsConstants.METADATE.equalsIgnoreCase(dbObject) && !schemaName.isEmpty()
//						&& !tableName.isEmpty()) {
//					return "SELECT column_name as name, data_type as type from information_schema.columns where table_schema='"
//							+ schemaName + "' \r\n" + "and table_name ='" + tableName + "'";
//				}
//			}
//
//		} catch (Exception e) {
//			log.error("Exception occurred while executing getSqlQuery method {}", e.getMessage());
//		}
//		return null;
//	}

	public static PreparedStatement getPreparedStatement(ConnectorProperties properties, String connectorType,
			String dbObject, String schemaName, String tableName,String sqlQuery) {
		try {
			if ("pgsql".equalsIgnoreCase(connectorType)) {
				Connection con = getConnectionObject(properties, connectorType);
				PreparedStatement preparedStatement;
				if (QsConstants.SCHEMA.equalsIgnoreCase(dbObject)) {
					preparedStatement = con.prepareStatement(
							"SELECT distinct table_schema  as name FROM information_schema.tables a join information_schema.schemata b\r\n"
									+ "on a.table_schema = b.schema_name WHERE table_type = 'BASE TABLE' and schema_owner !='rdsadmin' ORDER BY table_schema");
					return preparedStatement;
				} else if (QsConstants.TABLE.equalsIgnoreCase(dbObject) && !schemaName.isEmpty()) {
					preparedStatement = con.prepareStatement(
							"SELECT table_name as name FROM information_schema.tables a join information_schema.schemata b\r\n"
									+ "on a.table_schema = b.schema_name WHERE table_type = 'BASE TABLE' and schema_owner !='rdsadmin'\r\n"
									+ "and table_schema= ? ORDER BY table_name");
					preparedStatement.setString(1, schemaName);
					return preparedStatement;
				} else if (QsConstants.METADATE.equalsIgnoreCase(dbObject) && !schemaName.isEmpty()
						&& !tableName.isEmpty()) {
					preparedStatement = con.prepareStatement(
							"SELECT column_name as column_name, data_type as data_type from information_schema.columns where table_schema=?  and table_name = ?");
					preparedStatement.setString(1, schemaName);
					preparedStatement.setString(2, tableName);
					return preparedStatement;
				}else {
					preparedStatement = con.prepareStatement(sqlQuery);
					preparedStatement.setMaxRows(1);
					return preparedStatement;
				}
			}

		} catch (Exception e) {
			log.error("Exception occurred while executing getSqlQuery method {}", e.getMessage());
		}
		return null;
	}

	public static boolean exportCSVFile(final String sqlQuery, final String csvFilePath, DataBaseRequest request) {
		BufferedWriter writer = null;
		CSVPrinter csvWriter = null;
		File tmpCsvFileTmp = null;
		boolean isSuccess = true;
		ConnectorProperties properties = gson.fromJson(gson.toJsonTree(request.getConnectorConfig()),
				ConnectorProperties.class);
		try (Connection con = getConnectionObject(properties, request.getConnectorType());
				ResultSet result = con.prepareStatement(sqlQuery).executeQuery()) {

			tmpCsvFileTmp = new File(csvFilePath);
			if (!tmpCsvFileTmp.getParentFile().exists()) {
				tmpCsvFileTmp.getParentFile().mkdirs();
			}

			writer = new BufferedWriter(new FileWriter(csvFilePath));
			csvWriter = new CSVPrinter(writer, CSVFormat.EXCEL.withHeader(result));
			csvWriter.printRecords(result);
			log.info("Writing the records into the CSV File: {}", csvFilePath);

		} catch (IOException | SQLException ex) {
			log.error("Exception while downloading data {}", ex.getMessage());
			isSuccess = false;
		} finally {
			try {
				if (csvWriter != null) {
					csvWriter.close(true);
				}
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				log.error("Exception while closing the stream: {}", e.getMessage());
			}
		}
		return isSuccess;
	}

	public static boolean exportJSONFile(final String sqlQuery, final String jsonFilePath, DataBaseRequest request,
			ConnectorProperties properties, int records) {
		JsonWriter writer = null;
		File tmpJsonFileTmp = null;
		boolean isSuccess = true;
		try {
			Connection con = getConnectionObject(properties, request.getConnectorType());
			PreparedStatement preparedStatement = con.prepareStatement(sqlQuery);
			preparedStatement.setFetchSize(records);
			preparedStatement.setMaxRows(records);
			preparedStatement.setFetchDirection(ResultSet.FETCH_FORWARD);
			ResultSet result = preparedStatement.executeQuery();
			ResultSetMetaData md = result.getMetaData();
			int numCols = md.getColumnCount();
			List<String> colNames = IntStream.range(0, numCols).mapToObj(i -> {
				try {
					return md.getColumnName(i + 1);
				} catch (SQLException e) {
					log.error("Exception while closing the stream: {}", e.getMessage());
				}
				return null;
			}).collect(Collectors.toList());

			JSONArray resultJson = new JSONArray();
			while (result.next()) {
				JSONObject row = new JSONObject();
				colNames.forEach(cn -> {
					try {
						row.put(cn, result.getObject(cn));
					} catch (JSONException | SQLException e) {
						log.error("Exception while closing the stream: {}", e.getMessage());
					}
				});
				resultJson.put(row);
			}
			tmpJsonFileTmp = new File(jsonFilePath);
			if (!tmpJsonFileTmp.getParentFile().exists()) {
				tmpJsonFileTmp.getParentFile().mkdirs();
			}

			writer = new JsonWriter(new FileWriter(tmpJsonFileTmp));
			writer.jsonValue(resultJson.toString());
			writer.close();

			log.info("Writing the records into the JSON File: {}", jsonFilePath);

		} catch (IOException | SQLException ex) {
			log.error("Exception while downloading data {}", ex.getMessage());
			isSuccess = false;
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				log.error("Exception while closing the stream: {}", e.getMessage());
			}
		}
		return isSuccess;
	}
}
