package ai.quantumics.api.util;

import java.io.IOException;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;

import com.opencsv.ResultSetHelper;

public class CustomResultSetHelperService implements ResultSetHelper {
	public CustomResultSetHelperService() {

	}

	protected static final int CLOBBUFFERSIZE = 2048;

	static final String DEFAULT_DATE_FORMAT = "dd-MMM-yyyy";
	static final String DEFAULT_TIMESTAMP_FORMAT = "dd-MMM-yyyy HH:mm:ss";
	private static final String DEFAULT_VALUE = StringUtils.EMPTY;

	protected String dateFormat = DEFAULT_DATE_FORMAT;
	protected String dateTimeFormat = DEFAULT_TIMESTAMP_FORMAT;
	protected NumberFormat integerFormat;
	protected NumberFormat floatingPointFormat;
	private Map<String, Integer> desiredColumns = null;

	/**
	 * Set a default date format pattern that will be used by the service.
	 *
	 * @param dateFormat Desired date format
	 */
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * Set a default date time format pattern that will be used by the service.
	 *
	 * @param dateTimeFormat Desired date time format
	 */
	public void setDateTimeFormat(String dateTimeFormat) {
		this.dateTimeFormat = dateTimeFormat;
	}

	/**
	 * Set a default number formatter for floating point numbers that will be used
	 * by the service.
	 *
	 * @param format Desired number format. Should not be null
	 */
	public void setIntegerFormat(NumberFormat format) {
		this.integerFormat = format;
	}

	/**
	 * Set a default number formatter for integer numbers that will be used by the
	 * service.
	 *
	 * @param format Desired number format. Should not be null
	 */
	public void setFloatingPointFormat(NumberFormat format) {
		this.floatingPointFormat = format;
	}

	@Override
	public String[] getColumnNames(ResultSet rs) throws SQLException {
		ResultSetMetaData metadata = rs.getMetaData();
		String[] nameArray = new String[desiredColumns.size()];
		int j = 0;
		for (int i = 0; i < metadata.getColumnCount(); i++) {
			if (desiredColumns.get(metadata.getColumnLabel(i + 1)) != null) {
				nameArray[j] = metadata.getColumnLabel(i + 1);
				j++;
			}
		}
		return nameArray;
	}

	public void setDesiredColumns(ResultSet rs, String[] Columns) throws SQLException {
		desiredColumns = new HashMap<String, Integer>();
		ResultSetMetaData metadata = rs.getMetaData();
		for (int i = 0; i < metadata.getColumnCount(); i++) {
			if (Arrays.stream(Columns).anyMatch(metadata.getColumnLabel(i + 1)::equalsIgnoreCase)) {
				desiredColumns.put(metadata.getColumnLabel(i + 1), i + 1);
			}
		}
	}

	@Override
	public String[] getColumnValues(ResultSet rs) throws SQLException, IOException {
		return this.getColumnValues(rs, false, dateFormat, dateTimeFormat);
	}

	@Override
	public String[] getColumnValues(ResultSet rs, boolean trim) throws SQLException, IOException {
		return this.getColumnValues(rs, trim, dateFormat, dateTimeFormat);
	}

	@Override
	public String[] getColumnValues(ResultSet rs, boolean trim, String dateFormatString, String timeFormatString)
			throws SQLException, IOException {
		ResultSetMetaData metadata = rs.getMetaData();
		String[] valueArray = new String[desiredColumns.size()];
		int j = 0;
		;
		for (int i = 1; i <= metadata.getColumnCount(); i++) {
			if (desiredColumns.get(metadata.getColumnLabel(i)) != null) {
				valueArray[j] = getColumnValue(rs, metadata.getColumnType(i), i, trim, dateFormatString,
						timeFormatString);
				j++;
			}
		}
		return valueArray;
	}

	/**
	 * The formatted timestamp.
	 * 
	 * @param timestamp             Timestamp read from resultset
	 * @param timestampFormatString Format string
	 * @return Formatted time stamp.
	 */
	protected String handleTimestamp(Timestamp timestamp, String timestampFormatString) {
		SimpleDateFormat timeFormat = new SimpleDateFormat(timestampFormatString);
		return timestamp == null ? null : timeFormat.format(timestamp);
	}

	private String getColumnValue(ResultSet rs, int colType, int colIndex, boolean trim, String dateFormatString,
			String timestampFormatString) throws SQLException, IOException {

		String value;

		switch (colType) {
		case Types.BOOLEAN:
			value = Objects.toString(rs.getBoolean(colIndex));
			break;
		case Types.NCLOB:
			value = handleNClob(rs, colIndex);
			break;
		case Types.CLOB:
			value = handleClob(rs, colIndex);
			break;
		case Types.BIGINT:
			value = applyFormatter(integerFormat, rs.getBigDecimal(colIndex));
			break;
		case Types.DECIMAL:
		case Types.REAL:
		case Types.NUMERIC:
			value = applyFormatter(floatingPointFormat, rs.getBigDecimal(colIndex));
			break;
		case Types.DOUBLE:
			value = applyFormatter(floatingPointFormat, rs.getDouble(colIndex));
			break;
		case Types.FLOAT:
			value = applyFormatter(floatingPointFormat, rs.getFloat(colIndex));
			break;
		case Types.INTEGER:
		case Types.TINYINT:
		case Types.SMALLINT:
			value = applyFormatter(integerFormat, rs.getInt(colIndex));
			break;
		case Types.DATE:
			value = handleDate(rs, colIndex, dateFormatString);
			break;
		case Types.TIME:
			value = Objects.toString(rs.getTime(colIndex), DEFAULT_VALUE);
			break;
		case Types.TIMESTAMP:
			value = handleTimestamp(rs.getTimestamp(colIndex), timestampFormatString);
			break;
		case Types.NVARCHAR:
		case Types.NCHAR:
		case Types.LONGNVARCHAR:
			value = handleNVarChar(rs, colIndex, trim);
			break;
		case Types.LONGVARCHAR:
		case Types.VARCHAR:
		case Types.CHAR:
			value = handleVarChar(rs, colIndex, trim);
			break;
		default:
			// This takes care of Types.BIT, Types.JAVA_OBJECT, and anything
			// unknown.
			value = Objects.toString(rs.getObject(colIndex), DEFAULT_VALUE);
		}

		if (rs.wasNull() || value == null) {
			value = DEFAULT_VALUE;
		}

		return value;
	}

	private String applyFormatter(NumberFormat formatter, Number value) {
		if (value != null && formatter != null) {
			return formatter.format(value);
		}
		return Objects.toString(value, DEFAULT_VALUE);
	}

	/**
	 * retrieves the data from an VarChar in a result set
	 *
	 * @param rs       - result set
	 * @param colIndex - column location of the data in the result set
	 * @param trim     - should the value be trimmed before being returned
	 * @return a string representing the VarChar from the result set
	 * @throws SQLException
	 */
	protected String handleVarChar(ResultSet rs, int colIndex, boolean trim) throws SQLException {
		String value;
		String columnValue = rs.getString(colIndex);
		if (trim && columnValue != null) {
			value = columnValue.trim();
		} else {
			value = columnValue;
		}
		return value;
	}

	/**
	 * retrieves the data from an NVarChar in a result set
	 *
	 * @param rs       - result set
	 * @param colIndex - column location of the data in the result set
	 * @param trim     - should the value be trimmed before being returned
	 * @return a string representing the NVarChar from the result set
	 * @throws SQLException
	 */
	protected String handleNVarChar(ResultSet rs, int colIndex, boolean trim) throws SQLException {
		String value;
		String nColumnValue = rs.getNString(colIndex);
		if (trim && nColumnValue != null) {
			value = nColumnValue.trim();
		} else {
			value = nColumnValue;
		}
		return value;
	}

	/**
	 * retrieves an date from a result set
	 *
	 * @param rs               - result set
	 * @param colIndex         - column location of the data in the result set
	 * @param dateFormatString - desired format of the date
	 * @return - a string representing the data from the result set in the format
	 *         set in dateFomratString.
	 * @throws SQLException
	 */
	protected String handleDate(ResultSet rs, int colIndex, String dateFormatString) throws SQLException {
		String value = DEFAULT_VALUE;
		Date date = rs.getDate(colIndex);
		if (date != null) {
			SimpleDateFormat df = new SimpleDateFormat(dateFormatString);
			value = df.format(date);
		}
		return value;
	}

	/**
	 * retrieves the data out of a CLOB
	 *
	 * @param rs       - result set
	 * @param colIndex - column location of the data in the result set
	 * @return the data in the Clob as a string.
	 * @throws SQLException
	 * @throws IOException
	 */
	protected String handleClob(ResultSet rs, int colIndex) throws SQLException, IOException {
		String value = DEFAULT_VALUE;
		Clob c = rs.getClob(colIndex);
		if (c != null) {
			TextStringBuilder sb = new TextStringBuilder();
			sb.readFrom(c.getCharacterStream());
			value = sb.toString();
		}
		return value;
	}

	/**
	 * retrieves the data out of a NCLOB
	 *
	 * @param rs       - result set
	 * @param colIndex - column location of the data in the result set
	 * @return the data in the NCLOB as a string.
	 * @throws SQLException
	 * @throws IOException
	 */
	protected String handleNClob(ResultSet rs, int colIndex) throws SQLException, IOException {
		String value = DEFAULT_VALUE;
		NClob nc = rs.getNClob(colIndex);
		if (nc != null) {
			TextStringBuilder sb = new TextStringBuilder();
			sb.readFrom(nc.getCharacterStream());
			value = sb.toString();
		}
		return value;
	}
}
