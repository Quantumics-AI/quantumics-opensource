/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.misc;

import ai.quantumics.api.adapter.MsSqlAdapter;
import ai.quantumics.api.adapter.MySqlAdapter;
import ai.quantumics.api.adapter.OracleAdapter;
import ai.quantumics.api.adapter.PostgresAdapter;
import ai.quantumics.api.req.DataBaseRequest;
import ai.quantumics.api.req.DownloadDataRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Component
public class ExportCSV {

  @Value("${tmp.ext.location}")
  private String extLocation;

  /*public boolean exportFile(
      final String sqlQuery, final String csvFilePath, final DownloadDataRequest dataBaseRequest) {
    PreparedStatement stmt = null;
    BufferedWriter writer = null;
    boolean isSuccess = true;
    Connection conn = null;
    ResultSet resultSet = null;
    CSVPrinter csvWriter = null;
    try {
      conn = getDBConnection(dataBaseRequest);
      log.info("Database connection established...");
      stmt = conn.prepareStatement(sqlQuery);
      log.info("Executed the statement...");
      resultSet = stmt.executeQuery();
      
      checkFolderPath();
      writer = new BufferedWriter(new FileWriter(csvFilePath));
      csvWriter = new CSVPrinter(writer, CSVFormat.EXCEL.withHeader(resultSet));
      csvWriter.printRecords(resultSet);
      log.info("Writing the records into the CSV File: {}", csvFilePath);
    } catch (IOException | SQLException ex) {
      log.error("Exception while downloading data {}", ex.getMessage());
      isSuccess = false;
    } finally {
      if(csvWriter != null) {
        try {
          csvWriter.close(true);
        } catch (IOException e) {
          log.error("Exception while closing the stream: {}", e.getMessage());
        }
      }
      closeResources(stmt, writer, conn, resultSet);
    }
    return isSuccess;
  }

  private void checkFolderPath() {
    final File file = new File(extLocation);
    if (!file.exists() || !file.isDirectory()) {
      boolean mkdir = file.mkdir();
      log.info("Creating directory required to store data {}", mkdir);
    } else {
      log.info("Directory Path required to store download data is present");
    }
  }

  private void closeResources(
      PreparedStatement stmt, BufferedWriter writer, Connection conn, ResultSet rs) {
    try {
      if (writer != null) {
        writer.close();
      }
      if (rs != null) {
        rs.close();
      }
      if (stmt != null) {
        stmt.close();
      }
      if (conn != null) {
        conn.close();
      }
    } catch (SQLException | IOException ex) {
      log.error("Exception while closing resources {}", ex.getMessage());
    }
  }

  private Connection getDBConnection(final DownloadDataRequest downloadDataRequest) {
    Connection connection = null;
    final String dbType = downloadDataRequest.getDbType();
    DataBaseRequest dataBaseRequest = convertToDataBaseFormat(downloadDataRequest);
    switch (dbType) {
      case "pgsql":
        {
          connection = new PostgresAdapter(dataBaseRequest).getConnection();
          break;
        }
      case "oracle":
        {
          connection = new OracleAdapter(dataBaseRequest).getConnection();
          break;
        }
      case "mssql":
        {
          connection = new MsSqlAdapter(dataBaseRequest).getConnection();
          break;
        }
      case "mysql":
        {
          connection = new MySqlAdapter(dataBaseRequest).getConnection();
          break;
        }
      default:
        log.info("Error invalid db type");
    }
    return connection;
  }

  private DataBaseRequest convertToDataBaseFormat(DownloadDataRequest downloadRequest) {
    DataBaseRequest request = new DataBaseRequest();
    request.setHostName(downloadRequest.getHostName());
    request.setDbName(downloadRequest.getDbName());
    request.setDbType(downloadRequest.getDbType());
    request.setPassword(downloadRequest.getPassword());
    request.setPort(downloadRequest.getPort());
    request.setServiceName(downloadRequest.getServiceName());
    request.setUserName(downloadRequest.getUserName());
    return request;
  }*/
}
