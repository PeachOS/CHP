package com.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.postgresql.PGStatement;
import org.postgresql.ds.PGSimpleDataSource;

import com.test.DatabaseStatements;

public class DataBaseFunctions {
	static boolean loaded = false;
	static String URL;
	static String PORT;
	static String DATABASE;
	static String USER;
	static String PASSWORD;

	static PreparedStatement getOrderNonSummarizedStatement = null;

	static PreparedStatement getOrderSummarizedStatement = null;

	static PreparedStatement getDrugsStatement = null;

	static PreparedStatement addDrugStatement = null;

	static PreparedStatement getCategoryNamesStatement = null;

	static PreparedStatement updateInventoryStatenment = null;

	static PreparedStatement updateOrderStatusStatement = null;

	static PreparedStatement updateDrugStatement = null;

	static PreparedStatement addOrderStatement = null;

	private static PGSimpleDataSource pgSimpleDataSourceWeb = null;


	/**
	 * 
	 * @return A connection to the database, currently having all rights.
	 */
	public static Connection getWebConnection() {
		if (!loaded) {
			Properties prop = new Properties();
			try {
				// load a properties file from class path, inside static method
				prop.load(new FileInputStream("connection.properties"));

				URL = prop.getProperty("db_url");
				PORT = prop.getProperty("db_port");
				DATABASE = prop.getProperty("db_database");
				USER = prop.getProperty("db_user");
				PASSWORD = prop.getProperty("db_password");

			} catch (IOException ex) {
				ex.printStackTrace();
			}
			loaded = true;
		}
		try {
			if (pgSimpleDataSourceWeb == null) {
				pgSimpleDataSourceWeb = new PGSimpleDataSource();
				pgSimpleDataSourceWeb.setServerName(URL);
				pgSimpleDataSourceWeb.setPortNumber(Integer.valueOf(PORT));
				pgSimpleDataSourceWeb.setDatabaseName(DATABASE);
				pgSimpleDataSourceWeb.setUser(USER);
				pgSimpleDataSourceWeb.setPassword(PASSWORD);

			}
			Connection con = pgSimpleDataSourceWeb.getConnection();
			con.setAutoCommit(true);
			getOrderNonSummarizedStatement = con
					.prepareStatement(DatabaseStatements.GET_ORDER_NON_SUMMARIZED2);
			getOrderSummarizedStatement = con
					.prepareStatement(DatabaseStatements.GET_ORDER_SUMMARIZED2);
			getDrugsStatement = con
					.prepareStatement(DatabaseStatements.GET_DRUGS);
			addDrugStatement = con
					.prepareStatement(DatabaseStatements.ADD_DRUG);
			getCategoryNamesStatement = con
					.prepareStatement(DatabaseStatements.GET_CATEGORY_NAMES);
			updateInventoryStatenment = con
					.prepareStatement(DatabaseStatements.UPDATE_INVENTORY);
			updateOrderStatusStatement = con
					.prepareStatement(DatabaseStatements.UPDATE_ORDER_STATUS);
			updateDrugStatement = con
					.prepareStatement(DatabaseStatements.UPDATE_DRUG);
			addOrderStatement = con
					.prepareStatement(DatabaseStatements.ADD_ORDER_NEW);
			return con;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param con
	 *            Connection to be used
	 * @return JSONArray containing Categories, stored as JSONObjects
	 * @throws SQLException
	 */
	public static JSONArray getCategories(Connection con) {
		ResultSet resultSet;
		JSONArray result = null;
		try {
			System.out.println(getCategoryNamesStatement.toString());
			resultSet = getCategoryNamesStatement.executeQuery();
			result = ResultSetHelper.resultSetToJSONArray(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
	 * @return true if operation succeeded, false otherwise
	 * @throws SQLException
	 */
	public static boolean addOrder2(Connection con, JSONObject parameters) {
		if (parameters == null)
			return false;

		@SuppressWarnings("rawtypes")
		Set keySet = parameters.keySet();
		int keySize = keySet.size();

		if (keySize < 3) {
			System.err.println("Not enough Liis, try again!");
			return false;
		}
		String facility_idS = (String) parameters.get("facility_id");
		String order_statusS = (String) parameters.get("status");

		if (facility_idS == null || order_statusS == null)
			return false;

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
			addOrderStatement.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
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
	 * 
	 */
	public static JSONArray getDrugs(Connection con, JSONObject parameters) {

		String drug_idS = (String) parameters.get("drug_id");
		String category_idS = (String) parameters.get("category_id");

		String facility_idS = (String) parameters.get("facility_id");

		if (facility_idS == null)
			return null;

		try {
			Integer facility_id = Integer.valueOf(facility_idS);

			int p = 1;

			getDrugsStatement.setInt(p++, facility_id);

			if (drug_idS != null && drug_idS.matches("[0-9]*")) {
				Integer drug_id = Integer.valueOf(drug_idS);
				getDrugsStatement.setInt(p++, drug_id);
			} else
				getDrugsStatement.setNull(p++, Types.INTEGER);

			if (category_idS != null && category_idS.matches("[0-9]*")) {
				Integer category_id = Integer.valueOf(category_idS);
				getDrugsStatement.setInt(p++, category_id);
			} else
				getDrugsStatement.setNull(p++, Types.INTEGER);

			System.out.println(getDrugsStatement.toString());
			ResultSet rs = getDrugsStatement.executeQuery();

			return ResultSetHelper.resultSetToJSONArray(rs);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return new JSONArray();

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
	 */
	public static JSONArray getOrderSummary2(Connection con,
			JSONObject parameters) {
		if (parameters == null)
			return null;

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

			ResultSet rs = pstmt.executeQuery();

			return ResultSetHelper.resultSetToJSONArray(rs);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out
				.println("orderSummary finishes now. Whatever happens next is not its fault.");
		return resultArray;
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
	public static boolean updateOrderStatus(Connection con,
			JSONObject parameters) {
		if (parameters == null)
			return false;
		Integer order_id = Integer.valueOf((String) parameters.get("order_id"));
		Integer status = Integer.valueOf((String) parameters.get("status"));

		if (order_id == null || status == null)
			return false;

		try {
			updateOrderStatusStatement.setInt(1, status);
			updateOrderStatusStatement.setInt(2, order_id);

			updateOrderStatusStatement.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;

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
	 */
	public static boolean updateInventory(Connection con, JSONObject parameters) {
		if (parameters == null)
			return false;

		Integer facility_id = Integer.valueOf((String) parameters
				.get("facility_id"));

		if (facility_id == null)
			return false;

		@SuppressWarnings("unchecked")
		Set<Map.Entry<Object, Object>> a = parameters.entrySet();

		try {
			for (Iterator<Entry<Object, Object>> iterator = a.iterator(); iterator
					.hasNext();) {
				Entry<Object, Object> entry = iterator.next();
				String key = (String) entry.getKey();
				if (!key.isEmpty() && key.matches("[0-9]*")) {
					updateInventoryStatenment.setInt(1, facility_id);
					updateInventoryStatenment.setInt(2, Integer.valueOf(key));
					updateInventoryStatenment.setInt(3,
							Integer.valueOf((String) entry.getValue()));
					updateInventoryStatenment.executeQuery();
				}
			}
			return true;
		} catch (SQLException e) {
			e.getNextException().printStackTrace();
			e.printStackTrace();
		}

		return false;

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
	public static boolean addDrug(Connection con, JSONObject parameters) {
		String msdcodeS = (String) parameters.get("msdcode");
		String category_idS = (String) parameters.get("category_id");
		String med_name = (String) parameters.get("med_name");
		String common_name = (String) parameters.get("common_name");
		String unit = (String) parameters.get("unit");
		String unit_details = (String) parameters.get("unit_details");
		String unit_priceS = (String) parameters.get("unit_price");

		if (msdcodeS == null || category_idS == null || med_name == null
				|| unit_priceS == null)
			return false;

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

			System.out.println(addDrugStatement.toString());

			int result = addDrugStatement.executeUpdate();
			return result > 0;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
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
	public static boolean updateDrug(Connection con, JSONObject parameters) {

		String idS = (String) parameters.get("id");

		if (idS == null)
			return false;

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

			System.out.println(updateDrugStatement.toString());

			int result = updateDrugStatement.executeUpdate();

			return result > 0;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
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
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	private static void testGetDrugs(Connection con) {
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
	 */
	@SuppressWarnings({ "unchecked" })
	private static void testGetOrderSummary(Connection con) {
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
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	private static void testAddDrug(Connection con) {
		JSONObject input = new JSONObject();
		input.put("med_name", "Antimonogamysol");
		input.put("msdcode", "12345");
		input.put("category_id", "8");
		input.put("common_name", "Hippierol");
		input.put("unit", "Normalized Love Unit");
		input.put("unit_details", "3% Weed / NLU");
		input.put("unit_price", "1.2");

		boolean result = addDrug(con, input);
		System.out.println(Helper.niceJsonPrint(result, ""));

	}

	/**
	 * This function will print an exemplary Result of the
	 * {@link #updateDrug(Connection, JSONObject)} Function.
	 * 
	 * @param con
	 *            Connection to be used
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	private static void testUpdateDrug(Connection con) {
		JSONObject input = new JSONObject();
		input.put("category_id", "1");
		input.put("common_name", "Aspirin 3");
		input.put("id", "2");
		input.put("med_name", "Acetylsalicylic Acid");
		input.put("msdcode", "33");
		input.put("unit", "300mg");
		input.put("unit_details", "Tablet");
		input.put("unit_price", 3);

		boolean result = updateDrug(con, input);
		System.out.println(result);

	}

	/**
	 * This function will print an exemplary Result of the
	 * {@link #addOrder(Connection, JSONObject)} Function.
	 * 
	 * @param con
	 *            Connection to be used
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	private static void testAddOrder(Connection con) {
		Random rand = new Random();
		for (int a = 0; a < 40; a++) {
			JSONObject input = new JSONObject();
			input.put("facility_id", String.valueOf(rand.nextInt(3) + 1));
			input.put("status", "3");
			for (int i = 0; i < 1 + rand.nextInt(5); i++)
				input.put(String.valueOf(1 + rand.nextInt(50)),
						String.valueOf(1 + rand.nextInt(20)));

			boolean result = addOrder2(con, input);
		}
		for (int a = 0; a < 15; a++) {
			JSONObject input = new JSONObject();
			input.put("facility_id", String.valueOf(rand.nextInt(3) + 1));
			input.put("status", "2");
			for (int i = 0; i < 1 + rand.nextInt(5); i++)
				input.put(String.valueOf(1 + rand.nextInt(50)),
						String.valueOf(1 + rand.nextInt(20)));

			boolean result = addOrder2(con, input);
		}
		for (int a = 0; a < 10; a++) {
			JSONObject input = new JSONObject();
			input.put("facility_id", String.valueOf(rand.nextInt(3) + 1));
			input.put("status", "1");
			for (int i = 0; i < 1 + rand.nextInt(5); i++)
				input.put(String.valueOf(1 + rand.nextInt(50)),
						String.valueOf(1 + rand.nextInt(20)));

			boolean result = addOrder2(con, input);
		}
		for (int a = 0; a < 8; a++) {
			JSONObject input = new JSONObject();
			input.put("facility_id", String.valueOf(rand.nextInt(3) + 1));
			input.put("status", "4");
			for (int i = 0; i < 1 + rand.nextInt(5); i++)
				input.put(String.valueOf(1 + rand.nextInt(50)),
						String.valueOf(1 + rand.nextInt(20)));

			boolean result = addOrder2(con, input);
		}
	}

	private static void testGetCategories(Connection con) {
		JSONArray arr = getCategories(con);
		System.out.println(Helper.niceJsonPrint(arr, ""));
	}

	@SuppressWarnings({ "unused" })
	private static void tryNewStuff() {
		System.out.println("moep");

	}

	@SuppressWarnings({})
	public static void main(String[] args) {
		Connection con = getWebConnection();
		// testAddOrder(con);
		// testUpdateDrug(con);
		// testGetCategories(con);
		// testGetOrderSummary(con);
		// tryNewStuff();
		// testGetDrugs(con);
		// testAddDrug(con);
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
