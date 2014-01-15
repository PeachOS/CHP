package com.chp.dma;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.postgresql.PGStatement;
import org.postgresql.ds.PGSimpleDataSource;

import com.chp.dma.DatabaseStatements;

public class DataBaseFunctions {
	static boolean loaded = false;
	static String URL = "localhost";
	static String PORT = "5432";
	static String DATABASE = "chpv1_small";
	static String USER = "postgres";
	static String PASSWORD = "postgres";
	static Connection connection = null;

	static PreparedStatement getOrderNonSummarizedStatement = null;

	static PreparedStatement getOrderSummarizedStatement = null;

	static PreparedStatement getDrugsStatement = null;

	static PreparedStatement addDrugStatement = null;

	static PreparedStatement getCategoryNamesStatement = null;

	static PreparedStatement updateInventoryStatenment = null;

	static PreparedStatement updateOrderStatusStatement = null;

	static PreparedStatement updateDrugStatement = null;

	static PreparedStatement addOrderStatement = null;

	static PreparedStatement searchDrugsStatement = null;

	private static PGSimpleDataSource pgSimpleDataSourceWeb = null;

	/**
	 * 
	 * @return A connection to the database, currently having all rights.
	 * @throws SQLException
	 */
	public static Connection getWebConnection() throws SQLException {
		if (pgSimpleDataSourceWeb == null) {
			pgSimpleDataSourceWeb = new PGSimpleDataSource();
			pgSimpleDataSourceWeb.setServerName(URL);
			pgSimpleDataSourceWeb.setPortNumber(Integer.valueOf(PORT));
			pgSimpleDataSourceWeb.setDatabaseName(DATABASE);
			pgSimpleDataSourceWeb.setUser(USER);
			pgSimpleDataSourceWeb.setPassword(PASSWORD);

		}
		if (connection == null || connection.isClosed()) {
			try {
				connection = pgSimpleDataSourceWeb.getConnection();
				connection.setAutoCommit(true);
			} catch (SQLException e) {
				throw new SQLException(String.format(
						"Could not properly build a connection to Database.\n"
								+ "Function: getWebConnection()\n"
								+ "Details: %s\n"
								+ "pgSimpleDataSourceWeb == null: %B\n"
								+ "con == null: %B", e.getMessage(),
						pgSimpleDataSourceWeb == null, connection == null));
			}
			try {
				getOrderNonSummarizedStatement = connection
						.prepareStatement(DatabaseStatements.GET_ORDER_NON_SUMMARIZED2);
				getOrderSummarizedStatement = connection
						.prepareStatement(DatabaseStatements.GET_ORDER_SUMMARIZED2);
				getDrugsStatement = connection
						.prepareStatement(DatabaseStatements.GET_DRUGS);
				addDrugStatement = connection
						.prepareStatement(DatabaseStatements.ADD_DRUG);
				getCategoryNamesStatement = connection
						.prepareStatement(DatabaseStatements.GET_CATEGORY_NAMES);
				updateInventoryStatenment = connection
						.prepareStatement(DatabaseStatements.UPDATE_INVENTORY);
				updateOrderStatusStatement = connection
						.prepareStatement(DatabaseStatements.UPDATE_ORDER_STATUS);
				updateDrugStatement = connection
						.prepareStatement(DatabaseStatements.UPDATE_DRUG);
				addOrderStatement = connection
						.prepareStatement(DatabaseStatements.ADD_ORDER_NEW);
				searchDrugsStatement = connection
						.prepareStatement(DatabaseStatements.SEARCH_DRUGS);
			} catch (SQLException e) {
				throw new SQLException(String.format(
						"Could not prepare the statements.\n"
								+ "Function: getWebConnection()\n"
								+ "Details: %s", e.getMessage()));
			}
		}
		return connection;
	}

	/**
	 * 
	 * @param con
	 *            Connection to be used
	 * @return JSONArray containing Categories, stored as JSONObjects
	 * @throws SQLException
	 */
	public static JSONArray getCategories(Connection con) throws SQLException {
		ResultSet resultSet;
		JSONArray result = null;
		try {
			System.out.println(getCategoryNamesStatement.toString());
			resultSet = getCategoryNamesStatement.executeQuery();
		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Execution of Statement failed.\n"
							+ "Function: getCategories()\n" + "Statement: %s\n"
							+ "Details: %s",
					getCategoryNamesStatement.toString(), e.getMessage()));
		}
		result = ResultSetHelper.resultSetToJSONArray(resultSet);
		return result;
	}

	/**
	 * 
	 * @param con
	 *            Connection to be used
	 * @param parameters
	 *            JSON Object with the following parameters:<br>
	 *            facility_id : (int),<br>
	 *            status : (int),<br>
	 * <br>
	 *            Additionally Key-Value-Pairs in the form of (drug_id (int) :
	 *            unit_number (int)) will have to be added
	 * @throws SQLException
	 */
	public static void addOrder2(Connection con, JSONObject parameters)
			throws SQLException {

		@SuppressWarnings("rawtypes")
		Set keySet = parameters.keySet();

		String facility_idS = (String) parameters.get("facility_id");
		String order_statusS = (String) parameters.get("status");

		if (facility_idS == null || order_statusS == null)
			throw new IllegalArgumentException(String.format(
					"facility_id and order_status were not provided as parameter.\n"
							+ "Function: addOrder2()\n" + "Parameters: %s",
					Helper.niceJsonPrint(parameters, "")));

		Integer facility_id = Integer.valueOf(facility_idS);
		Integer status = Integer.valueOf(order_statusS);

		ArrayDeque<String> orderBlas = new ArrayDeque<String>();
		for (Object keyO : keySet) {
			String key = keyO.toString();
			String val = parameters.get(keyO).toString();

			if (!key.isEmpty() && key.matches("[0-9]*") && !val.isEmpty()
					&& val.matches("[0-9]*")) {
				Integer drug_id = Integer.valueOf(key);
				Integer number = Integer.valueOf(val);
				if (number <= 0)
					continue;
				orderBlas.add("(" + drug_id + "," + number + ")");
				System.out.println("Parameters fround: " + drug_id + "|"
						+ number);
			}

		}

		try {
			int p = 1;
			addOrderStatement.setInt(p++, facility_id);
			addOrderStatement.setInt(p++, status);

			Array a = con.createArrayOf("order", orderBlas.toArray());
			addOrderStatement.setArray(3, a);
			System.out.println(addOrderStatement.toString());
		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Adding parameters to the statement failed\n"
							+ "Function: addOrder2()\n" + "Parameters: %s\n"
							+ "Details: %s",
					Helper.niceJsonPrint(parameters, ""), e.getMessage()));
		}
		try {
			addOrderStatement.executeUpdate();
		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Execution of Statement failed.\n"
							+ "Function: addOrder2()\n" + "Statement: %s\n"
							+ "Parameters: %s\n" + "Details: %s",
					addOrderStatement.toString(),
					Helper.niceJsonPrint(parameters, ""), e.getMessage()));
		}
	}

	/**
	 * 
	 * @param con
	 *            Connection to be used
	 * @param parameters
	 *            JSONObject with the following parameters:<br>
	 *            Mandatory:<br>
	 *            facility_id : (int)<br>
	 *            Optional:<br>
	 *            drug_id : (int),<br>
	 *            category_id : (int)
	 * @return JSONArray containing Drugs, stored as JSONObjects
	 * @throws SQLException
	 * 
	 */
	public static JSONArray getDrugs(Connection con, JSONObject parameters)
			throws SQLException {

		String drug_idS = (String) parameters.get("drug_id");
		String category_idS = (String) parameters.get("category_id");

		String facility_idS = (String) parameters.get("facility_id");

		if (facility_idS == null)
			throw new IllegalArgumentException(String.format(
					"facility_id was not provided as parameter.\n"
							+ "Function: getDrugs()\n" + "Parameters: %s",
					Helper.niceJsonPrint(parameters, "")));

		try {
			Integer facility_id = Integer.valueOf(facility_idS);

			int p = 1;

			getDrugsStatement.setInt(p++, facility_id);

			if (drug_idS != null) {
				Integer drug_id = Integer.valueOf(drug_idS);
				getDrugsStatement.setInt(p++, drug_id);
			} else
				getDrugsStatement.setNull(p++, Types.INTEGER);

			if (category_idS != null) {
				Integer category_id = Integer.valueOf(category_idS);
				getDrugsStatement.setInt(p++, category_id);
			} else
				getDrugsStatement.setNull(p++, Types.INTEGER);
		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Adding parameters to the statement failed\n"
							+ "Function: getDrugs()\n" + "Statement: %s\n"
							+ "Parameters: %s\n" + "Details: %s",
					getDrugsStatement.toString(),
					Helper.niceJsonPrint(parameters, ""), e.getMessage()));
		}
		ResultSet rs;
		try {
			System.out.println(getDrugsStatement.toString());
			rs = getDrugsStatement.executeQuery();

		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Execution of Statement failed.\n"
							+ "Function: getDrugs()\n" + "Statement: %s\n"
							+ "Parameters: %s\n" + "Details: %s",
					getDrugsStatement.toString(),
					Helper.niceJsonPrint(parameters, ""), e.getMessage()));
		}
		JSONArray arr = ResultSetHelper.resultSetToJSONArray(rs);
		System.out.println(Helper.niceJsonPrint(arr, ""));
		return arr;

	}

	/**
	 * 
	 * @param con
	 * @param parameters
	 *            JSONObject with the following OPTIONAL parameters:<br>
	 *            name : (String), msdcode : (int)
	 * @return
	 * @throws SQLException
	 */
	public static JSONArray searchDrugs(Connection con, JSONObject parameters)
			throws SQLException {
		Object name = parameters.get("name");
		Object msdCodeS = parameters.get("msdcode");

		try {
			int p = 1;
			if (msdCodeS != null && msdCodeS.toString().matches("[0-9]*")) {
				searchDrugsStatement.setInt(p++,
						Integer.valueOf(msdCodeS.toString()));
			} else {
				searchDrugsStatement.setNull(p++, Types.INTEGER);
			}

			if (name != null) {
				searchDrugsStatement.setString(p++, name.toString());
				searchDrugsStatement.setString(p++, name.toString());
			} else {
				searchDrugsStatement.setNull(p++, Types.VARCHAR);
				searchDrugsStatement.setNull(p++, Types.VARCHAR);
			}
		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Adding parameters to the statement failed\n"
							+ "Function: searchDrugs()\n" + "Statement: %s\n"
							+ "Parameters: %s\n" + "Details: %s",
					searchDrugsStatement.toString(),
					Helper.niceJsonPrint(parameters, ""), e.getMessage()));
		}
		ResultSet rs;
		try {
			System.out.println(searchDrugsStatement.toString());
			rs = searchDrugsStatement.executeQuery();

		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Execution of Statement failed.\n"
							+ "Function: searchDrugs()\n" + "Statement: %s\n"
							+ "Parameters: %s\n" + "Details: %s",
					searchDrugsStatement.toString(),
					Helper.niceJsonPrint(parameters, ""), e.getMessage()));
		}

		return ResultSetHelper.resultSetToJSONArray(rs);
	}

	/**
	 * 
	 * @param con
	 *            Connection to be used
	 * @param parameters
	 *            JSON Object with the following parameters:<br>
	 *            order_id (int),<br>
	 *            order_start (Timestamp: yyyy-[m]m-[d]d hh:mm:ss),<br>
	 *            order_end (Timestamp: yyyy-[m]m-[d]d hh:mm:ss),<br>
	 *            order_status (one of: 1 (initiated),2 (sent),3 (delivered),
	 *            4(canceled)<br>
	 *            facility_id (int),<br>
	 *            facility_name (String)
	 * @return
	 * @throws SQLException
	 */
	public static JSONArray getOrderSummary2(Connection con,
			JSONObject parameters) throws SQLException {

		String order_id = (String) parameters.get("order_id");

		String order_start_String = (String) parameters.get("order_start");
		Timestamp order_start = order_start_String == null ? null
				: java.sql.Timestamp.valueOf(order_start_String);
		String order_end_String = (String) parameters.get("order_end");
		Timestamp order_end = order_end_String == null ? null
				: java.sql.Timestamp.valueOf(order_end_String);
		String order_status = (String) parameters.get("order_status");

		Integer facility_id = Integer.valueOf((String) parameters
				.get("facility_id"));

		String summarizeS = (String) parameters.get("summarize");
		boolean summarize = summarizeS == null ? false : Boolean
				.valueOf(summarizeS);

		PreparedStatement pstmt = summarize ? getOrderSummarizedStatement
				: getOrderNonSummarizedStatement;
		JSONArray resultArray = null;
		try {
			int p = 1;

			if (order_start != null)
				pstmt.setTimestamp(p++, order_start);
			else
				pstmt.setTimestamp(p++, new Timestamp(
						PGStatement.DATE_NEGATIVE_INFINITY));

			if (order_end != null)
				pstmt.setTimestamp(p++, order_end);
			else
				pstmt.setTimestamp(p++, new Timestamp(
						PGStatement.DATE_POSITIVE_INFINITY));

			if (order_id != null)
				pstmt.setInt(p++, Integer.valueOf(order_id));
			else
				pstmt.setNull(p++, Types.INTEGER);

			if (order_status != null)
				pstmt.setInt(p++, Integer.valueOf(order_status));
			else
				pstmt.setNull(p++, Types.INTEGER);

			if (facility_id != null)
				pstmt.setInt(p++, facility_id);
			else
				pstmt.setNull(p++, Types.INTEGER);

			System.out.println(pstmt.toString());

		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Adding parameters to the statement failed\n"
							+ "Function: getOrderSummary2()\n"
							+ "Parameters: %s\n" + "Details: %s",
					Helper.niceJsonPrint(parameters, ""), e.getMessage()));
		}
		ResultSet rs;
		try {
			rs = pstmt.executeQuery();

		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Execution of Statement failed.\n"
							+ "Function: getOrderSummary2()\n"
							+ "Statement: %s\n" + "Parameters: %s\n"
							+ "Details: %s", pstmt.toString(),
					Helper.niceJsonPrint(parameters, ""), e.getMessage()));
		}
		return ResultSetHelper.resultSetToJSONArray(rs);

	}

	/**
	 * 
	 * @param con
	 *            Connection to be used
	 * @param parameters
	 *            JSON Object with the following parameters:<br>
	 *            order_id (int),<br>
	 *            status (int),<br>
	 * @return true if operation succeeded, false otherwise
	 * @throws SQLException
	 */
	public static void updateOrderStatus(Connection con, JSONObject parameters)
			throws SQLException {
		Integer order_id = Integer.valueOf((String) parameters.get("order_id"));
		Integer status = Integer.valueOf((String) parameters.get("status"));

		if (order_id == null || status == null)
			throw new IllegalArgumentException(String.format(
					"order_id and status were not provided as parameters.\n"
							+ "Function: updateOrderStatus\n"
							+ "Parameters: %s",
					Helper.niceJsonPrint(parameters, "")));

		try {
			updateOrderStatusStatement.setInt(1, status);
			updateOrderStatusStatement.setInt(2, order_id);

		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Adding parameters to the statement failed\n"
							+ "Function: updateOrderStatus()\n"
							+ "Parameters: %s\n" + "Details: %s",
					Helper.niceJsonPrint(parameters, ""), e.getMessage()));
		}
		try {
			updateOrderStatusStatement.executeUpdate();
		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Execution of Statement failed.\n"
							+ "Function: updateOrderStatus()\n"
							+ "Statement: %s\n" + "Parameters: %s\n"
							+ "Details: %s",
					updateOrderStatusStatement.toString(),
					Helper.niceJsonPrint(parameters, ""), e.getMessage()));
		}

	}

	/**
	 * 
	 * @param con
	 *            Connection to be used
	 * @param parameters
	 *            JSON Object with the following parameters:<br>
	 *            facility_id : (int),<br>
	 * <br>
	 *            Additionally Key-Value-Pairs in the form of (drug_id (int) :
	 *            difference (int)) will have to be added
	 * @return true if operation succeeded, false otherwise
	 * @throws SQLException
	 */
	public static void updateInventory(Connection con, JSONObject parameters)
			throws SQLException {

		Integer facility_id = Integer.valueOf((String) parameters
				.get("facility_id"));

		if (facility_id == null)
			throw new IllegalArgumentException(String.format(
					"facility_id was not provided as parameter.\n"
							+ "Function: updateInventory()\n"
							+ "Parameters: %s",
					Helper.niceJsonPrint(parameters, "")));

		@SuppressWarnings("unchecked")
		Set<Map.Entry<Object, Object>> a = parameters.entrySet();

		for (Iterator<Entry<Object, Object>> iterator = a.iterator(); iterator
				.hasNext();) {
			Entry<Object, Object> entry = iterator.next();
			String key = (String) entry.getKey();
			if (!key.isEmpty() && key.matches("[0-9]*")) {
				try {
					updateInventoryStatenment.setInt(1, facility_id);
					updateInventoryStatenment.setInt(2, Integer.valueOf(key));
					updateInventoryStatenment.setInt(3,
							Integer.valueOf((String) entry.getValue()));
				} catch (SQLException e) {
					throw new SQLException(String.format(
							"Adding parameters to the statement failed\n"
									+ "Function: updateInventory()\n"
									+ "Parameters: %s\n" + "Details: %s",
							Helper.niceJsonPrint(parameters, ""),
							e.getMessage()));
				}
				try {
					updateInventoryStatenment.executeQuery();
				} catch (SQLException e) {
					throw new SQLException(String.format(
							"Execution of Statement failed.\n"
									+ "Function: updateInventory()\n"
									+ "Statement: %s\n" + "Parameters: %s\n"
									+ "Details: %s",
							updateInventoryStatenment.toString(),
							Helper.niceJsonPrint(parameters, ""),
							e.getMessage()));
				}
			}

		}

	}

	/**
	 * 
	 * @param con
	 *            Connection to be used
	 * @param parameters
	 *            JSON Object with the following parameters:<br>
	 *            Mandatory:<br>
	 *            msdcode (int),<br>
	 *            category_id (int),<br>
	 *            med_name (String),<br>
	 *            unit_price (Double)<br>
	 *            Optional:<br>
	 *            common_name (String),<br>
	 *            unit (String),<br>
	 *            unit_details (String)
	 * @return true if operation succeeded, false otherwise
	 * @throws SQLException
	 */
	public static void addDrug(Connection con, JSONObject parameters)
			throws SQLException {
		String msdcodeS = (String) parameters.get("msdcode");
		String category_idS = (String) parameters.get("category_id");
		String med_name = (String) parameters.get("med_name");
		String common_name = (String) parameters.get("common_name");
		String unit = (String) parameters.get("unit");
		String unit_details = (String) parameters.get("unit_details");
		String unit_priceS = (String) parameters.get("unit_price");

		if (msdcodeS == null || category_idS == null || med_name == null
				|| unit_priceS == null)
			throw new IllegalArgumentException(String.format(
					"msdcode, category_id, med_name and unit_price are mandatory parameters.\n"
							+ "Function: addDrug()\n" + "Parameters: %s",
					Helper.niceJsonPrint(parameters, "")));

		Double unit_price = Double.valueOf(unit_priceS);

		try {
			int p = 1;

			addDrugStatement.setInt(p++, Integer.valueOf(msdcodeS));

			addDrugStatement.setInt(p++, Integer.valueOf(category_idS));

			addDrugStatement.setString(p++, med_name);

			for (String parameter : new String[] { common_name, unit,
					unit_details }) {
				if (parameter == null)
					addDrugStatement.setNull(p++, java.sql.Types.VARCHAR);
				else
					addDrugStatement.setString(p++, parameter);
			}

			addDrugStatement.setDouble(p++, unit_price);
		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Adding parameters to the statement failed\n"
							+ "Function: addDrug()\n" + "Parameters: %s\n"
							+ "Details: %s",
					Helper.niceJsonPrint(parameters, ""), e.getMessage()));
		}
		try {
			System.out.println(addDrugStatement.toString());

			addDrugStatement.executeUpdate();

		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Execution of Statement failed.\n"
							+ "Function: addDrug()\n" + "Statement: %s\n"
							+ "Parameters: %s\n" + "Details: %s",
					addDrugStatement.toString(),
					Helper.niceJsonPrint(parameters, ""), e.getMessage()));
		}
	}

	/**
	 * 
	 * @param con
	 *            Connection to be used
	 * @param parameters
	 *            JSON Object with the following parameters:<br>
	 *            Mandatory:<br>
	 *            id (int)<br>
	 *            Optional:<br>
	 *            msdcode (int),<br>
	 *            category_id (int),<br>
	 *            med_name (String),<br>
	 *            common_name (String),<br>
	 *            unit (String),<br>
	 *            unit_details (String),<br>
	 *            unit_price (Double)<br>
	 * @return true if operation succeeded, false otherwise
	 * @throws SQLException
	 */
	public static void updateDrug(Connection con, JSONObject parameters)
			throws SQLException {

		String idS = (String) parameters.get("id");

		if (idS == null)
			throw new IllegalArgumentException(
					"id was not provided as parameter.\n"
							+ "Function: updateDrug()");

		int id = Integer.valueOf(idS);

		String msdcodeS = (String) parameters.get("msdcode");
		String category_idS = (String) parameters.get("category_id");
		String med_name = (String) parameters.get("med_name");
		String common_name = (String) parameters.get("common_name");
		String unit = (String) parameters.get("unit");
		String unit_details = (String) parameters.get("unit_details");
		String unit_priceS = String.valueOf(parameters.get("unit_price"));

		try {
			int p = 1;
			if (msdcodeS != null)
				updateDrugStatement.setInt(p++, Integer.valueOf(msdcodeS));
			else
				updateDrugStatement.setNull(p++, Types.INTEGER);

			if (category_idS != null)
				updateDrugStatement.setInt(p++, Integer.valueOf(category_idS));
			else
				updateDrugStatement.setNull(p++, Types.INTEGER);

			if (med_name != null)
				updateDrugStatement.setString(p++, med_name);
			else
				updateDrugStatement.setNull(p++, Types.VARCHAR);

			if (common_name != null)
				updateDrugStatement.setString(p++, common_name);
			else
				updateDrugStatement.setNull(p++, Types.VARCHAR);

			if (unit != null)
				updateDrugStatement.setString(p++, unit);
			else
				updateDrugStatement.setNull(p++, Types.VARCHAR);

			if (unit_details != null)
				updateDrugStatement.setString(p++, unit_details);
			else
				updateDrugStatement.setNull(p++, Types.VARCHAR);

			if (unit_priceS != null)
				updateDrugStatement.setDouble(p++, Double.valueOf(unit_priceS));
			else
				updateDrugStatement.setNull(p++, Types.DOUBLE);

			updateDrugStatement.setInt(p++, id);

		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Adding parameters to the statement failed\n"
							+ "Function: updateDrug()\n" + "Details: %s",
					e.getMessage()));
		}
		try {
			System.out.println(updateDrugStatement.toString());

			updateDrugStatement.executeUpdate();

		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Execution of Statement failed.\n"
							+ "Function: updateDrug()\n" + "Statement: %s\n"
							+ "Details: %s", updateDrugStatement.toString(),
					e.getMessage()));
		}
	}

	/**
	 * The following section is just for testing single functions.
	 * 
	 * 
	 */

	/**
	 * This function will print an exemplary Result of the
	 * {@link #getDrugs(Connection, JSONObject)} Function.
	 * 
	 * @param con
	 *            Connection to be used
	 * @throws SQLException
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	private static void testGetDrugs(Connection con) throws SQLException {
		JSONObject input = new JSONObject();
		input.put("facility_id", "1");
		input.put("drug_id", "4");
		JSONArray result = getDrugs(con, input);
		System.out.println(result);
		System.out.println(Helper.niceJsonPrint(result, ""));
	}

	/**
	 * This function will print an exemplary Result of the
	 * {@link #getOrderSummary(Connection, JSONObject)} Function.
	 * 
	 * @param con
	 *            Connection to be used
	 * @throws SQLException
	 */
	@SuppressWarnings({ "unchecked" })
	private static void testGetOrderSummary(Connection con) throws SQLException {
		JSONObject input = new JSONObject();
		input.put("facility_id", "1");
		input.put("summarize", "true");
		// input.put("order_start", "2013-09-21 00:00:00");
		JSONArray result = getOrderSummary2(con, input);
		try {
			FileWriter fw = new FileWriter(new File("testJSON.txt"));
			result.writeJSONString(fw);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(Helper.niceJsonPrint(result, ""));
		// result = getOrderSummary(con, input);
		// System.out.println(result.toJSONString());
	}

	/**
	 * This function will print an exemplary Result of the
	 * {@link #addDrug(Connection, JSONObject)} Function.
	 * 
	 * @param con
	 *            Connection to be used
	 * @throws SQLException
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	private static void testAddDrug(Connection con) throws SQLException {
		JSONObject input = new JSONObject();
		input.put("med_name", "Antimonogamysol");
		input.put("msdcode", "12345");
		input.put("category_id", "8");
		input.put("common_name", "Hippierol");
		input.put("unit", "Normalized Love Unit");
		input.put("unit_details", "3% Weed / NLU");
		input.put("unit_price", "1.2");

		addDrug(con, input);

	}

	/**
	 * This function will print an exemplary Result of the
	 * {@link #updateDrug(Connection, JSONObject)} Function.
	 * 
	 * @param con
	 *            Connection to be used
	 * @throws SQLException
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	private static void testUpdateDrug(Connection con) throws SQLException {
		JSONObject input = new JSONObject();
		input.put("category_id", "1");
		input.put("common_name", "Aspirin 3");
		input.put("id", "2");
		input.put("med_name", "Acetylsalicylic Acid");
		input.put("msdcode", "33");
		input.put("unit", "300mg");
		input.put("unit_details", "Tablet");
		input.put("unit_price", 3);

		updateDrug(con, input);

	}

	/**
	 * This function will print an exemplary Result of the
	 * {@link #addOrder(Connection, JSONObject)} Function.
	 * 
	 * @param con
	 *            Connection to be used
	 * @throws SQLException
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	private static void testAddOrder(Connection con) throws SQLException {
		Random rand = new Random();
		for (int a = 0; a < 40; a++) {
			JSONObject input = new JSONObject();
			input.put("facility_id", String.valueOf(rand.nextInt(3) + 1));
			input.put("status", "3");
			for (int i = 0; i < 1 + rand.nextInt(5); i++)
				input.put(String.valueOf(1 + rand.nextInt(50)),
						String.valueOf(1 + rand.nextInt(20)));

			addOrder2(con, input);
		}
		for (int a = 0; a < 15; a++) {
			JSONObject input = new JSONObject();
			input.put("facility_id", String.valueOf(rand.nextInt(3) + 1));
			input.put("status", "2");
			for (int i = 0; i < 1 + rand.nextInt(5); i++)
				input.put(String.valueOf(1 + rand.nextInt(50)),
						String.valueOf(1 + rand.nextInt(20)));

			addOrder2(con, input);
		}
		for (int a = 0; a < 10; a++) {
			JSONObject input = new JSONObject();
			input.put("facility_id", String.valueOf(rand.nextInt(3) + 1));
			input.put("status", "1");
			for (int i = 0; i < 1 + rand.nextInt(5); i++)
				input.put(String.valueOf(1 + rand.nextInt(50)),
						String.valueOf(1 + rand.nextInt(20)));

			addOrder2(con, input);
		}
		for (int a = 0; a < 8; a++) {
			JSONObject input = new JSONObject();
			input.put("facility_id", String.valueOf(rand.nextInt(3) + 1));
			input.put("status", "4");
			for (int i = 0; i < 1 + rand.nextInt(5); i++)
				input.put(String.valueOf(1 + rand.nextInt(50)),
						String.valueOf(1 + rand.nextInt(20)));

			addOrder2(con, input);
		}
	}

	private static void testGetCategories(Connection con) throws SQLException {
		JSONArray arr = getCategories(con);
		System.out.println(Helper.niceJsonPrint(arr, ""));
	}

	@SuppressWarnings({ "unused" })
	private static void tryNewStuff() throws SQLException {
		JSONObject input = new JSONObject();
		input.put("name", "s");
		JSONArray arr = searchDrugs(getWebConnection(), input);
		System.out.println(Helper.niceJsonPrint(arr, ""));

	}

	@SuppressWarnings({})
	public static void main(String[] args) {
		try {
			Connection con = getWebConnection();
			// testAddOrder(con);
			// testUpdateDrug(con);
//			 testGetCategories(con);
//			 testGetOrderSummary(con);
//			tryNewStuff();
			 testGetDrugs(con);
			// testAddDrug(con);
			con.close();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

}
