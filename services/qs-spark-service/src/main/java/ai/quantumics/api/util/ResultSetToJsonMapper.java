/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;

// import org.springframework.jdbc.support.JdbcUtils;

public class ResultSetToJsonMapper {
	public static JSONArray mapResultSet(final ResultSet rs) throws SQLException, JSONException {
		final JSONArray jArray = new JSONArray();
		JSONObject jsonObject;
		final ResultSetMetaData rsmd = rs.getMetaData();
		final int columnCount = rsmd.getColumnCount();
		while (rs.next()) {
			jsonObject = new JSONObject();
			for (int index = 1; index <= columnCount; index++) {
				final String column = rsmd.getColumnName(index);
				final Object value = rs.getObject(column);
				if (value == null) {
					jsonObject.put(column, "");
				} else if (value instanceof Integer) {
					jsonObject.put(column, value);
				} else if (value instanceof String) {
					jsonObject.put(column, value);
				} else if (value instanceof Boolean) {
					jsonObject.put(column, value);
				} else if (value instanceof Date) {
					jsonObject.put(column, ((Date) value).getTime());
				} else if (value instanceof Long) {
					jsonObject.put(column, value);
				} else if (value instanceof Double) {
					jsonObject.put(column, value);
				} else if (value instanceof Float) {
					jsonObject.put(column, value);
				} else if (value instanceof BigDecimal) {
					jsonObject.put(column, value);
				} else if (value instanceof Byte) {
					jsonObject.put(column, value);
				} else if (value instanceof byte[]) {
					jsonObject.put(column, value);
				} else {
					throw new IllegalArgumentException("Illegal mapping object of type: " + value.getClass());
				}
			}
			jArray.put(jsonObject);
		}
		return jArray;
	}

	public static JSONArray getJSONDataFromResultSet(ResultSet rs) throws SQLException, Exception {
		ResultSetMetaData metaData = rs.getMetaData();
		int count = metaData.getColumnCount();
		String[] columnName = new String[count];
		JSONArray jsonArray = new JSONArray();
		while (rs.next()) {
			JSONObject jsonObject = new JSONObject();
			for (int i = 1; i <= count; i++) {
				columnName[i - 1] = metaData.getColumnLabel(i);
				jsonObject.put(columnName[i - 1], rs.getObject(i));
			}
			jsonArray.put(jsonObject);
		}
		return jsonArray;
	}

	public static JSONArray getJSONMetadaDataFromResultSet(ResultSet rs) throws SQLException, Exception {
		ResultSetMetaData metaData = rs.getMetaData();
		int count = metaData.getColumnCount();
		JSONArray jsonArray = new JSONArray();
		while (rs.next()) {
			for (int i = 1; i <= count; i++) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("column_name", metaData.getColumnName(i));
				jsonObject.put("data_type", metaData.getColumnTypeName(i));
				jsonArray.put(jsonObject);
			}
		}
		return jsonArray;
	}
}
