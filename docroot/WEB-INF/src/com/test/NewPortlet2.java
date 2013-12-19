package com.test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Logger;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.liferay.portal.kernel.servlet.ServletResponseUtil;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;

/**
 * Portlet implementation class NewPortlet2
 */
public class NewPortlet2 extends MVCPortlet {

	private static DateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss]");
	private static Logger logger = Logger.getLogger("InfoLogging");
	
	private static JSONObject requestToJSONObject(ResourceRequest request) {
		JSONObject result = new JSONObject();
		Enumeration<String> parametersE = request.getParameterNames();
		while (parametersE.hasMoreElements()) {
			String parameter = parametersE.nextElement();
			String value = request.getParameter(parameter);
			result.put(parameter, value);
		}
		
		return result;
	}
	
	private static void writeMessage(ResourceResponse response, JSONObject jsonObject) throws IOException {
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
		httpResponse.setContentType("application/json;charset=UTF-8");
		ServletResponseUtil.write(httpResponse, jsonObject.toJSONString());	
	}

	private static void writeMessage(ResourceResponse response, JSONArray jsonArray) throws IOException {
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
		httpResponse.setContentType("application/json;charset=UTF-8");
		ServletResponseUtil.write(httpResponse, jsonArray.toJSONString());
	}

	private static void writeMessage(ResourceResponse response, String string) throws IOException {
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
		httpResponse.setContentType("application/plain;charset=UTF-8");
		ServletResponseUtil.write(httpResponse, string);	
	}
	
	
 
	public void updateBook(ResourceRequest ResourceRequest,
			ResourceResponse ResourceResponse)
			throws IOException, PortletException {
			String bookTitle = ParamUtil.getString(ResourceRequest, "bookTitle");
			String author = ParamUtil.getString(ResourceRequest, "author");
			System.out.println("Your inputs ==> " + bookTitle + ", " + author);
			}
	

	// DMA functions
	
	/**
	 * 
	 * @param request Ignored
	 * @param response Will contain a JSONArray, containing JSONObjects like:<br>
	 * category_id (int)<br>
	 * category_name (String)
	 * @throws PortletException
	 * @throws IOException
	 */
	
	public void getDrugCategories(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {
		
		System.out.println("getDrugCategories reached");
		Date date = new Date();
		System.out.println(dateFormat.format(date));
		JSONObject parameters = requestToJSONObject(request);
		
		JSONArray list;
		Connection con = null;
		try {
			con = DataBaseFunctions.getWebConnection();
			list = DataBaseFunctions.getCategories(con);
		} catch (SQLException e) {	
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		} finally {
			try {con.close();} catch (SQLException e) {e.printStackTrace();}
		}
		
		System.out.println("getDrugCategories response: " + list.toJSONString());
		writeMessage(response,list);
	}
	
	/**
	 * 
	 * @param request
	 * 				Parameters:<br>
	 *              Mandatory:<br>
	 *            	facility_id : (int)<br>
	 *            	Optional:<br>
	 *            	drug_id : (int),<br>
	 *            	category_id : (int)<br>
	 * 				index : (int)
	 * @param response
	 * @throws PortletException
	 * @throws IOException
	 */
	
	public void getDrugs(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {
		Date date = new Date();
		System.out.println(dateFormat.format(date));
		JSONObject parameters = requestToJSONObject(request);
		JSONArray list;
		Connection con = null;
		try {
			con = DataBaseFunctions.getWebConnection();
			list = DataBaseFunctions.getDrugs(con,parameters);
		} catch (SQLException e) {
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		} finally {
			try {con.close();} catch (SQLException e) {e.printStackTrace();}
		}
		
		
		if (parameters.get("index") != null) {
			
			for (int i = 0; i < list.size(); i++) {
				((JSONObject) (list.get(i))).put("index", parameters.get("index"));
			}
		}
		writeMessage(response,list);
	
		
	}

	/**
	 * 
	 * @param request
	 *            Optional parameters:<br>
	 *            name (String, will be compared to medical and common name of drugs),<br>
	 *            msdcode (int)
	 * @param response
	 * @throws PortletException
	 * @throws IOException
	 */
	
	public void searchDrugs(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {
		Date date = new Date();
		System.out.println(dateFormat.format(date));
		
		JSONObject parameters = requestToJSONObject(request);
		Connection con = null;
		JSONArray list;
		try {
			con = DataBaseFunctions.getWebConnection();
			list = DataBaseFunctions.searchDrugs(con,parameters);
		} catch (SQLException e) {
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		} finally {
			try {con.close();} catch (SQLException e) {e.printStackTrace();}
		}
		writeMessage(response,list);
	}
	
	/**
	 * 
	 * @param request
	 *            Possible parameters:<br>
	 *            order_id (int),<br>
	 *            order_start (String/Timestamp: yyyy-[m]m-[d]d hh:mm:ss),<br>
	 *            order_end (String/Timestamp: yyyy-[m]m-[d]d hh:mm:ss),<br>
	 *            order_status (String, one of: 'initiated','sent','delivered','canceled'<br>
	 *            facility_id (int),<br>
	 *            facility_name (String), <br>
	 *            summarize (Boolean)
	 * @param response
	 * @throws PortletException
	 * @throws IOException
	 */
	
	public void getOrderSummary(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {
		Date date = new Date();
		System.out.println(dateFormat.format(date));
		
		JSONObject parameters = requestToJSONObject(request);
		Connection con = null;
		JSONArray list;
		try {
			con = DataBaseFunctions.getWebConnection();
			list = DataBaseFunctions.getOrderSummary2(con,parameters);
		} catch (SQLException e) {
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		} finally {
			try {con.close();} catch (SQLException e) {e.printStackTrace();}
		}
		writeMessage(response,list);
	}

	/**
	 * 
	 * @deprecated Replaced by {@link #getOrderSummary()}. Add "sent" as order_status to achieve same functionality.
	 */
	
	public void getSentOrderSummary(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {

		JSONObject parameters = requestToJSONObject(request);
		parameters.put("order_status", "sent");
		Connection con = null;
		JSONArray list;
		try {
			con = DataBaseFunctions.getWebConnection();
			list = DataBaseFunctions.getOrderSummary2(con,parameters);
		} catch (SQLException e) {
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		} finally {
			try {con.close();} catch (SQLException e) {e.printStackTrace();}
		}
		writeMessage(response,list);
		
	}
	

	/**
	 * 
	 * @param request
	 *            Parameters:<br>
	 *            facility_id : (int),<br>
	 * <br>
	 *            Additionally Key-Value-Pairs in the form of (drug_id (int) :
	 *            difference (int)) will have to be added
	 * @param response
	 *            1 if query successful, 0 otherwise
	 * @throws PortletException
	 * @throws IOException
	 */
	
	public void updateStock(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {
		Date date = new Date();
		System.out.println(dateFormat.format(date));

		JSONObject parameters = requestToJSONObject(request);
		Connection con = null;
		try {
			con = DataBaseFunctions.getWebConnection();
			DataBaseFunctions.updateInventory(con,parameters);
		} catch (SQLException e) {
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		} finally {
			try {con.close();} catch (SQLException e) {e.printStackTrace();}
		}
		writeMessage(response,"1");
		
		
	}
	
	/**
	 * 
	 * @param request
	 *            Parameters:<br>
	 *            order_id (int),<br>
	 *            status (String),<br>
	 * @param response
	 *            1 if query successful, 0 otherwise
	 * @throws PortletException
	 * @throws IOException
	 */
	
	public void updateOrder(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {
		Date date = new Date();
		System.out.println(dateFormat.format(date));


		JSONObject parameters = requestToJSONObject(request);
		Connection con = null;
		try {
			con = DataBaseFunctions.getWebConnection();
			DataBaseFunctions.updateOrderStatus(con,parameters);
		} catch (SQLException e) {
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		} finally {
			try {con.close();} catch (SQLException e) {e.printStackTrace();}
		}
		writeMessage(response,"1");
		
	}
	
	/**
	 * 
	 * @param request
	 *            Parameters:<br>
	 *            Mandatory:<br>
	 *            msdcode (int),<br>
	 *            category_id (int),<br>
	 *            med_name (String),<br>
	 *            unit_price (Double)<br>
	 *            Optional:<br>
	 *            common_name (String),<br>
	 *            unit (String),<br>
	 *            unit_details (String)
	 * @param response
	 * @throws PortletException
	 * @throws IOException
	 */
	
	public void addNewDrug(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {

		Date date = new Date();
		System.out.println(dateFormat.format(date));
		JSONObject parameters = requestToJSONObject(request);		
		Connection con = null;
		try {
			con = DataBaseFunctions.getWebConnection();
			DataBaseFunctions.addDrug(con,parameters);
		} catch (SQLException e) {
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		} finally {
			try {con.close();} catch (SQLException e) {e.printStackTrace();}
		}
		writeMessage(response,"1");
		
	}
	
	/**
	 * 
	 * @param request
	 *            Parameters:<br>
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
	 * @param response
	 * @throws PortletException
	 * @throws IOException
	 */
	
	public void updateDrug(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {
		Date date = new Date();
		System.out.println(dateFormat.format(date));

		JSONObject parameters = requestToJSONObject(request);
		Connection con = null;
		try {
			con = DataBaseFunctions.getWebConnection();
			DataBaseFunctions.updateDrug(con,parameters);
		} catch (SQLException e) {
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		} finally {
			try {con.close();} catch (SQLException e) {e.printStackTrace();}
		}
		writeMessage(response,"1");
	}
	

	/**
	 * 
	 * @param request
	 *            Parameters:<br>
	 *            facility_id : (int),<br>
	 *            status : (int),<br>
	 * <br>
	 *            Additionally Key-Value-Pairs in the form of (drug_id (int) :
	 *            unit_number (int)) will have to be added
	 */
	
	public void sendOrder(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {
		Date date = new Date();
		System.out.println(dateFormat.format(date));

		JSONObject parameters = requestToJSONObject(request);
		Connection con = null;
		try {
			con = DataBaseFunctions.getWebConnection();
			DataBaseFunctions.addOrder2(con,parameters);
		} catch (SQLException e) {
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		} finally {
			try {con.close();} catch (SQLException e) {e.printStackTrace();}
		}
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
		httpResponse.setContentType("application/json;charset=UTF-8");
		ServletResponseUtil.write(httpResponse, "1");
		
	}

		
	// Very necessary function, please don't delete anything in here
	@Override
    public void processAction(
            ActionRequest actionRequest, ActionResponse actionResponse)
        throws IOException, PortletException {
		
		System.out.println("processAction reached");
		
        PortletPreferences prefs = actionRequest.getPreferences();
        String actionName = actionRequest.getParameter("actionName");
        boolean switchJSP = false;
        
        if (actionName != null) {
        	
        	
        	if (actionName.equals("subCategories")) {
        		
        		String catId1 = actionRequest.getParameter("category_id");
                if (catId1 != null) {
                    actionRequest.setAttribute("category_id", catId1);
                }
        	}
        	else if (actionName.equals("materials")) {
        		System.out.println("materials");
        		String catId2 = actionRequest.getParameter("id");
                if (catId2 != null) {
                    actionRequest.setAttribute("mat_id", catId2);
                }
        	}
        	else if (actionName.equals("goToMaterial")) {
        		Map<String,String[]> params = actionRequest.getParameterMap();
        		for (String key : params.keySet()) {
        			System.out.println(key + ": " + params.get(key));
        		}
        	}
        	else if (actionName.equals("subQuestions")) {
        		String questionId = actionRequest.getParameter("question_id");
                if (questionId != null) {
                	actionRequest.setAttribute("question_id", questionId);
                }
                
                String title = actionRequest.getParameter("title");
                if (title != null) {
                	actionRequest.setAttribute("title", title);
                }
                
                // hackery
                String next = actionRequest.getParameter("question_id");
                if (next != null && next.equals("3")) {
                	System.out.println("switching...");
                	switchJSP = true;
                }
        	}
        }
        
        String jspPage = actionRequest.getParameter("jspPage");
        System.out.println("jspPage: " + jspPage);
        if (jspPage != null) {
        	System.out.println(jspPage);
        	actionResponse.setRenderParameter("jspPage", jspPage);
        }
        if (switchJSP) {
        	actionResponse.setRenderParameter("jspPage", "/html/newportlet2/diagnose.jsp");
        }
        
        super.processAction(actionRequest, actionResponse);
    }
	
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {        
				
		String resourceID = request.getResourceID();
		 if ("getDrugCategories".equals(resourceID)) {
			 getDrugCategories(request, response);
		 }
		 
		 if ("getDrugs".equals(resourceID)) {
			 getDrugs(request, response);
		 }
		 
		 if ("getOrderSummary".equals(resourceID)) {
			 getOrderSummary(request, response);
		 }
		 
		 if ("updateStock".equals(resourceID)) {
			 updateStock(request, response);
		 }
		 
		 if ("updateOrder".equals(resourceID)) {
			 updateOrder(request, response);
		 }
		 
		 if ("addNewDrug".equals(resourceID)) {
			 addNewDrug(request, response);
		 }
		 
		 if ("updateDrug".equals(resourceID)) {
			 updateDrug(request, response);
		 }
		 
		 if ("sendOrder".equals(resourceID)) {
			 sendOrder(request, response);
		 }
	}
}

