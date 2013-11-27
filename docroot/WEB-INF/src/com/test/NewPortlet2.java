package com.test;

import com.test.DataBaseFunctions;

import java.io.IOException;
import java.sql.Connection;
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
import javax.portlet.ProcessAction;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.servlet.ServletResponseUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;

/**
 * Portlet implementation class NewPortlet2
 */
public class NewPortlet2 extends MVCPortlet {

	private static DateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss]");
	private static Logger logger = Logger.getLogger("InfoLogging");
	
	private static JSONObject requestToJSONObject(ActionRequest request) {
		JSONObject result = new JSONObject();
		Enumeration<String> parametersE = request.getParameterNames();
		while (parametersE.hasMoreElements()) {
			String parameter = parametersE.nextElement();
			String value = request.getParameter(parameter);
			result.put(parameter, value);
		}
		
		return result;
	}
	
	private static void writeMessage(ActionResponse response, JSONObject jsonObject) throws IOException {
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
		httpResponse.setContentType("application/json;charset=UTF-8");
		ServletResponseUtil.write(httpResponse, jsonObject.toJSONString());	
	}

	private static void writeMessage(ActionResponse response, JSONArray jsonArray) throws IOException {
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
		httpResponse.setContentType("application/json;charset=UTF-8");
		ServletResponseUtil.write(httpResponse, jsonArray.toJSONString());	
	}
	
	
 
	public void updateBook(ActionRequest actionRequest,
			ActionResponse actionResponse)
			throws IOException, PortletException {
			String bookTitle = ParamUtil.getString(actionRequest, "bookTitle");
			String author = ParamUtil.getString(actionRequest, "author");
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
	@ProcessAction(name = "getDrugCategories")
	public void getDrugCategories(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		Date date = new Date();
		System.out.println(dateFormat.format(date));
		JSONObject parameters = requestToJSONObject(request);
		Connection con = DataBaseFunctions.getWebConnection();
		if (con==null) {
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", "getWebConnection returned null when called in "+"getDrugCategories()");
			writeMessage(response,errorObject);
			return;
		}
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
		httpResponse.setContentType("application/json;charset=UTF-8");
		JSONArray list = DataBaseFunctions.getCategories(con);
		
		ServletResponseUtil.write(httpResponse, list.toJSONString());
		
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
	@SuppressWarnings("unchecked")
	@ProcessAction(name = "getDrugs")
	public void getDrugs(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		Date date = new Date();
		System.out.println(dateFormat.format(date));
		JSONObject parameters = requestToJSONObject(request);
		Connection con = DataBaseFunctions.getWebConnection();
		

		if (con==null) {
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", "getWebConnection returned null when called in "+"getDrugs()");
			writeMessage(response,errorObject);
			return;
		}
		
		JSONArray list = DataBaseFunctions.getDrugs(con,parameters);
		
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
	@ProcessAction(name = "getOrderSummary")
	public void getOrderSummary(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		Date date = new Date();
		System.out.println(dateFormat.format(date));
		
		JSONObject parameters = requestToJSONObject(request);
		Connection con = DataBaseFunctions.getWebConnection();

		if (con==null) {
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", "getWebConnection returned null when called in "+"getOrderSummary()");
			writeMessage(response,errorObject);
			return;
		}
		JSONArray list = DataBaseFunctions.getOrderSummary2(con,parameters);

		writeMessage(response,list);
		
	}

	/**
	 * 
	 * @deprecated Replaced by {@link #getOrderSummary()}. Add "sent" as order_status to achieve same functionality.
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	@ProcessAction(name = "getSentOrderSummary")
	public void getSentOrderSummary(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {

		JSONObject parameters = requestToJSONObject(request);
		parameters.put("order_status", "sent");
		Connection con = DataBaseFunctions.getWebConnection();

		if (con==null) {
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", "getWebConnection returned null when called in "+"getSentOrderSummary()");
			writeMessage(response,errorObject);
			return;
		}

		JSONArray list = DataBaseFunctions.getOrderSummary2(con,parameters);
		writeMessage(response,list);
		
	}
	
	/**
	 * 
	 * @deprecated Replaced by {@link #getOrderSummary()}. Add "order_id" and "summarize = false" as parameters to achieve same functionality.
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	@ProcessAction(name = "getOrderItems")
	public void getOrderItems(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {

		JSONObject parameters = requestToJSONObject(request);
		parameters.put("summarize", "false");
		Connection con = DataBaseFunctions.getWebConnection();
		if (con==null) {
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", "getWebConnection returned null when called in "+"getOrderItems()");
			writeMessage(response,errorObject);
			return;
		}
		
		JSONArray list = DataBaseFunctions.getOrderSummary2(con,parameters);

		writeMessage(response,list);
		
	}

	/**
	 * 
	 * @deprecated Replaced by {@link #getOrderSummary()}. Add "order_status : sent", "order_id" and "summarize : false" as parameters to achieve same functionality.
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	@ProcessAction(name = "getSentOrderItems")
	public void getSentOrderItems(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {

		JSONObject parameters = requestToJSONObject(request);
		parameters.put("order_status", "sent");
		parameters.put("summarize", "false");
		Connection con = DataBaseFunctions.getWebConnection();
		if (con==null) {
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", "getWebConnection returned null when called in "+"getSentOrderItems()");
			writeMessage(response,errorObject);
			return;
		}
		JSONArray list = DataBaseFunctions.getOrderSummary2(con,parameters);

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
	@ProcessAction(name = "updateStock")
	public void updateStock(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		Date date = new Date();
		System.out.println(dateFormat.format(date));

		JSONObject parameters = requestToJSONObject(request);
		Connection con = DataBaseFunctions.getWebConnection();
		if (con==null) {
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", "getWebConnection returned null when called in "+"updateStock()");
			writeMessage(response,errorObject);
			return;
		}
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
		httpResponse.setContentType("application/json;charset=UTF-8");
		boolean result = DataBaseFunctions.updateInventory(con,parameters);

		ServletResponseUtil.write(httpResponse, result?"1":"0");
		
		
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
	@ProcessAction(name = "updateOrder")
	public void updateOrder(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		Date date = new Date();
		System.out.println(dateFormat.format(date));


		JSONObject parameters = requestToJSONObject(request);
		Connection con = DataBaseFunctions.getWebConnection();
		if (con==null) {
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", "getWebConnection returned null when called in "+"updateOrder()");
			writeMessage(response,errorObject);
			return;
		}
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
		httpResponse.setContentType("application/json;charset=UTF-8");
		boolean result = DataBaseFunctions.updateOrderStatus(con,parameters);
		

		ServletResponseUtil.write(httpResponse, result?"1":"0");
		
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
	@ProcessAction(name = "addNewDrug")
	public void addNewDrug(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {

		Date date = new Date();
		System.out.println(dateFormat.format(date));
		JSONObject parameters = requestToJSONObject(request);		
		Connection con = DataBaseFunctions.getWebConnection();
		if (con==null) {
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", "getWebConnection returned null when called in "+"addNewDrug()");
			writeMessage(response,errorObject);
			return;
		}
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
		httpResponse.setContentType("application/json;charset=UTF-8");
		boolean result = DataBaseFunctions.addDrug(con,parameters);
		

		ServletResponseUtil.write(httpResponse, result?"1":"0");
		
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
	@ProcessAction(name = "updateDrug")
	public void updateDrug(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		Date date = new Date();
		System.out.println(dateFormat.format(date));

		JSONObject parameters = requestToJSONObject(request);
		Connection con = DataBaseFunctions.getWebConnection();
		if (con==null) {
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", "getWebConnection returned null when called in "+"updateDrug()");
			writeMessage(response,errorObject);
			return;
		}
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
		httpResponse.setContentType("application/json;charset=UTF-8");
		boolean result = DataBaseFunctions.updateDrug(con,parameters);
		

		ServletResponseUtil.write(httpResponse, result?"1":"0");
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
	@ProcessAction(name = "sendOrder")
	public void sendOrder(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		Date date = new Date();
		System.out.println(dateFormat.format(date));

		JSONObject parameters = requestToJSONObject(request);
		Connection con = DataBaseFunctions.getWebConnection();
		if (con==null) {
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", "getWebConnection returned null when called in "+"sendOrder()");
			writeMessage(response,errorObject);
			return;
		}
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
		httpResponse.setContentType("application/json;charset=UTF-8");
		boolean result = DataBaseFunctions.addOrder2(con,parameters);

		ServletResponseUtil.write(httpResponse, result?"1":"0");
		
	}

	
	// On-the-job training functions
	
	public void search(ActionRequest actionRequest,
			ActionResponse actionResponse)
			throws IOException, PortletException {
	
		System.out.println("searching...");
	}
	
	@SuppressWarnings("unchecked")
	@ProcessAction(name = "getTopCategories")
	public void getTopCategories(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		
		JSONArray list = new JSONArray();
        JSONObject cat1 = new JSONObject();
        JSONObject cat2 = new JSONObject();
        JSONObject cat3 = new JSONObject();
        JSONObject cat4 = new JSONObject();
        JSONObject cat5 = new JSONObject();
        cat1.put("id", 10);
        cat1.put("name", "Children");
        cat2.put("id", 20);
        cat2.put("name", "Pregnant women");
        cat3.put("id", 30);
        cat3.put("name", "Elderly");
        cat4.put("id", 40);
        cat4.put("name", "Infections");
        cat5.put("id", 50);
        cat5.put("name", "Mental health");
        list.add(cat1);
        list.add(cat2);
        list.add(cat3);
        list.add(cat4);
        list.add(cat5);
		
		 HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
         httpResponse.setContentType("application/json;charset=UTF-8");
         ServletResponseUtil.write(httpResponse, list.toJSONString());
		
	}
	
	@SuppressWarnings("unchecked")
	@ProcessAction(name = "getSubCategories")
	public void getSubCategories(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		Date date = new Date();
		System.out.println(dateFormat.format(date));
		
		String catId = request.getParameter("category_id");
		
		// IMPORTANT do not delete
		String index = request.getParameter("index");
		
		JSONArray list = new JSONArray();
        JSONObject cat1 = new JSONObject();
        JSONObject cat2 = new JSONObject();
        JSONObject cat3 = new JSONObject();
        JSONObject cat4 = new JSONObject();
        JSONObject cat5 = new JSONObject();
        cat1.put("id", 10);
        cat1.put("name", "Clinical disorders");
        cat2.put("id", 20);
        cat2.put("name", "Relationships");
        cat3.put("id", 30);
        cat3.put("name", "Medical and Developmental Disorders");
        cat4.put("id", 40);
        cat4.put("name", "Psychosocial Stressors");
        cat5.put("id", 50);
        cat5.put("name", "Emotional and Social Functioning");
        list.add(cat1);
        list.add(cat2);
        list.add(cat3);
        list.add(cat4);
        list.add(cat5);
		
        JSONObject responseJSON = new JSONObject();
        responseJSON.put("index", index);
        responseJSON.put("objects", list);
        
        HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
        httpResponse.setContentType("application/json;charset=UTF-8");
        ServletResponseUtil.write(httpResponse, responseJSON.toJSONString());
		
	}
	
	@SuppressWarnings("unchecked")
	@ProcessAction(name = "getMaterialTitles")
	public void getMaterialTitles(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		
		// IMPORTANT do not delete
		String index = request.getParameter("index");
		
		JSONArray list = new JSONArray();
		JSONObject mat1 = new JSONObject();
		JSONObject mat2 = new JSONObject();
		JSONObject mat3 = new JSONObject();
		mat1.put("id", "11");
		mat1.put("title", "Post-Traumatic Stress Disorder");
		mat2.put("id", "22");
		mat2.put("title", "Adjustment Disorder");
		mat3.put("id", "33");
		mat3.put("title", "Sleeping Behaviour Disorder");
		list.add(mat1);
		list.add(mat2);
		list.add(mat3);
		
		JSONObject responseJSON = new JSONObject();
        responseJSON.put("index", index);
        responseJSON.put("objects", list);
		
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
        httpResponse.setContentType("application/json;charset=UTF-8");
        ServletResponseUtil.write(httpResponse, responseJSON.toJSONString());
	}
	
	@SuppressWarnings("unchecked")
	@ProcessAction(name = "getMaterialContent")
	public void getMaterialContent(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		
		JSONObject contentObj = new JSONObject();
		contentObj.put("id", "33");
		contentObj.put("title", "Sleeping behaviour disorder");
		contentObj.put("text", 
				"A sleep disorder, or somnipathy, is a medical disorder of the sleep patterns of a person or animal." +
				" Some sleep disorders are serious enough to interfere with normal physical, mental and emotional " +
				"functioning. Polysomnography is a test commonly ordered for some sleep disorders. Disruptions in " +
				"sleep can be caused by a variety of issues, from teeth grinding (bruxism) to night terrors. When a" +
				" person suffers from difficulty falling asleep and staying asleep with no obvious cause, it is referred " +
				"to as insomnia.[1] Dyssomnia refers to a group of sleep disorders with the symptoms of trouble falling " +
				"asleep or maintaining sleep, which may cause an elevated sense of sleepiness during the day. Insomnia is " +
				"characterized by an extended period of symptoms including trouble with retaining sleep, fatigue," +
				" decreased attentiveness, and dysphoria. To diagnose insomnia, these symptoms must persist for a minimum " +
				"of 4 weeks. The DSM-IV categorizes insomnias into primary insomnia, insomnia associated with medical " +
				"or mental diseases, and insomnia associated with the consumption or abuse of substances. Individuals with" +
				" insomnia often worry about the negative health consequences, which can lead to the development of anxiety" +
				" and depression.[2] In addition, sleep disorders may also cause sufferers to sleep excessively, " +
				"a condition known as hypersomnia. Management of sleep disturbances that are secondary to mental," +
				" medical, or substance abuse disorders should focus on the underlying conditions.");
		
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
        httpResponse.setContentType("application/json;charset=UTF-8");
        ServletResponseUtil.write(httpResponse, contentObj.toJSONString());
	}
	
	
	@SuppressWarnings("unchecked")
	@ProcessAction(name = "getTopQuestions")
	public void getTopQuestions(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		
		
		JSONArray list = new JSONArray();
		JSONObject q1 = new JSONObject();
		JSONObject q2 = new JSONObject();
		JSONObject q3 = new JSONObject();
		q1.put("id", "11");
		q1.put("question", "Classify cough or difficult breathing");
		q2.put("id", "22");
		q2.put("question", "Classify diarrhea");
		q3.put("id", "33");
		q3.put("question", "Classify fever");
		list.add(q1);
		list.add(q2);
		list.add(q3);
		
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
        httpResponse.setContentType("application/json;charset=UTF-8");
        ServletResponseUtil.write(httpResponse, list.toJSONString());
	}
	
	@SuppressWarnings("unchecked")
	@ProcessAction(name = "getSubQuestions")
	public void getSubQuestions(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {

		Date date = new Date();
		System.out.println(dateFormat.format(date));
		System.out.println("getSubQuestions");
		JSONObject responseJSON = new JSONObject();
		JSONArray list = new JSONArray();
		
		if (request.getParameter("question_id").equals("2")) {
			
			responseJSON.put("title", "Cough or difficult breathing classified");
			responseJSON.put("next", "3");
			JSONObject q1 = new JSONObject();
			JSONObject q2 = new JSONObject();
			JSONObject q3 = new JSONObject();
			q1.put("id", "11");
			q1.put("question", "Are there any general danger signs present?");
			q1.put("description", "Child is unable to drink or breastfeed;child vomits everything; child has had or is having convulsions; " +
					"child is lethargic or unconscious.");
			q2.put("id", "22");
			q2.put("question", "Is there chest indrawing?");
			q2.put("description", "If present, give a trial of rapid acting inhaled bronchodilator for up to three times 15-20 minutes apart. " +
					"Count the breaths again and look for chest indrawing again, then classify.");
			q3.put("id", "33");
			q3.put("question", "Is there stridor in a calm child?");
			list.add(q1);
			list.add(q2);
			list.add(q3);
		}
		else if (request.getParameter("question_id").equals("3")) {
			getTreatment(request, response);
			return;
		}
		else {
			responseJSON.put("title", "Classify cough or difficult breathing");
			responseJSON.put("next", "2");
			JSONObject q1 = new JSONObject();
			JSONObject q2 = new JSONObject();
			q1.put("id", "11");
			q1.put("question", "Does the child have fast breathing?");
			q1.put("description", "If the child is 2-12 months old and they breathe 50 breaths per minute or more; " +
					"if the child 1-5 years old and they breathe 40 breaths per minute or more <p/>");
			q2.put("id", "22");
			q2.put("question", "Does the child have a cough?");
			list.add(q1);
			list.add(q2);
		}
		
		responseJSON.put("questions", list);
			
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
	    httpResponse.setContentType("application/json;charset=UTF-8");
	    ServletResponseUtil.write(httpResponse, responseJSON.toJSONString());
	}
	
	@SuppressWarnings("unchecked")
	@ProcessAction(name = "getTreatment")
	public void getTreatment(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		
		JSONObject responseJSON = new JSONObject();
		responseJSON.put("title","Severe pneumonia or very severe disease");
		responseJSON.put("treatment", "Give first dose of an appropriate antibiotic; Refer URGENTLY to hospital.");
		
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
	    httpResponse.setContentType("application/json;charset=UTF-8");
	    ServletResponseUtil.write(httpResponse, responseJSON.toJSONString());
	
	}
		
	// Very necessary function, please don't delete anything in here
	@Override
    public void processAction(
            ActionRequest actionRequest, ActionResponse actionResponse)
        throws IOException, PortletException {
		
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
        if (jspPage != null) {
        	System.out.println(jspPage);
        	actionResponse.setRenderParameter("jspPage", jspPage);
        }
        if (switchJSP) {
        	actionResponse.setRenderParameter("jspPage", "/html/newportlet2/diagnose.jsp");
        }
        
        super.processAction(actionRequest, actionResponse);
    }

}

