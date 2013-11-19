package com.test;

class DatabaseStatements {
	static final String GET_ORDER_SUMMARIZED2 = "SELECT "
			+ "o.id AS Order_ID, "
			+ "o.timestamp AS order_timestamp, "
			+ "o.status AS order_status, "
			+ "o.facility_id AS facility_id, "
			+ "sum(d.unit_price*unit_number(o.order_tup)) as total_costs "
			+ "FROM ("
			+ "SELECT facility_id,id,timestamp,status,unnest(order_arr) as order_tup "
			+ "FROM orders_new) o " + "JOIN drugs d "
			+ "ON drug_id(o.order_tup) = d.id " + "JOIN categories c "
			+ "ON c.id = d.category_id " + "WHERE o.timestamp BETWEEN ? AND ? "
			+ "AND o.id = COALESCE(?,o.id) "
			+ "AND o.status = COALESCE(?,o.status) "
			+ "AND o.facility_id = COALESCE(?,o.facility_id) "
			+ "GROUP BY o.facility_id,o.id,o.timestamp,o.status "
			+ "ORDER BY o.id ASC";

	static final String GET_ORDER_NON_SUMMARIZED2 = "SELECT "
			+ "o.id AS Order_ID, "
			+ "o.timestamp AS order_timestamp, "
			+ "o.status AS order_status, "
			+ "o.facility_id AS facility_id, "
			+ "array_to_json(array_agg(row_to_json((d.*,unit_number(o.order_tup))::drug_ext)))::text AS drugs "
			+ "FROM ("
			+ "SELECT facility_id,id,timestamp,status,unnest(order_arr) as order_tup "
			+ "FROM orders_new) o " + "JOIN drugs d "
			+ "ON drug_id(o.order_tup) = d.id " + "JOIN categories c "
			+ "ON c.id = d.category_id " + "WHERE o.timestamp BETWEEN ? AND ? "
			+ "AND o.id = COALESCE(?,o.id) "
			+ "AND o.status = COALESCE(?,o.status) "
			+ "AND o.facility_id = COALESCE(?,o.facility_id) "
			+ "GROUP BY o.facility_id,o.id,o.timestamp,o.status "
			+ "ORDER BY o.id ASC";

	static final String UPDATE_INVENTORY = "SELECT update_inventory(?,?,?)";

	static final String UPDATE_ORDER_STATUS = "UPDATE orders "
			+ "SET status = ? " + "WHERE id = ?";

	static final String GET_CATEGORY_NAMES = "SELECT c.id AS category_id,c.name AS category_name "
			+ "FROM categories c";

	static final String GET_DRUGS = "SELECT d.*,COALESCE(i.unit_number,0) as unit_number "
			+ "FROM drugs d "
			+ "LEFT OUTER JOIN (SELECT * FROM inventories WHERE facility_id = ?) i "
			+ "ON (d.id = i.drug_id) "
			+ "WHERE d.id = COALESCE(?,d.id) "
			+ "AND d.category_id = COALESCE(?,d.category_id) "
			+ "ORDER BY d.med_name ASC";

	static final String ADD_DRUG = "INSERT INTO drugs(id, msdcode, "
			+ "category_id, med_name, common_name, unit, unit_details, unit_price) "
			+ "VALUES (default, ?, ?, ?, ?, ?, ?, ?)";

	static final String ADD_ORDER_NEW = "INSERT INTO "
			+ "orders_new (id,facility_id,timestamp,status,order_arr) "
			+ "VALUES (default,?,now(),?,?)";

	static final String UPDATE_DRUG = "UPDATE drugs " + "SET "
			+ "msdcode = COALESCE(?,msdcode), "
			+ "category_id = COALESCE(?,category_id), "
			+ "med_name = COALESCE(?,med_name), "
			+ "common_name = COALESCE(?,common_name), "
			+ "unit = COALESCE(?,unit), "
			+ "unit_details = COALESCE(?,unit_details), "
			+ "unit_price = COALESCE(?,unit_price) " + "WHERE id = ?";

}
