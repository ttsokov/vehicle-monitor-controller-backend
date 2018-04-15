package org.ttsokov.vehicle_monitor_controller.backend.servlets;

import static java.net.HttpURLConnection.HTTP_OK;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ttsokov.vehicle_monitor_controller.backend.data_manager.DataManager;
import org.ttsokov.vehicle_monitor_controller.backend.model.Vehicle;
import org.ttsokov.vehicle_monitor_controller.utils.Utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.core.connectivity.api.DestinationException;
import com.sap.core.connectivity.api.http.HttpDestination;

public class Analytics extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String ANALYTICS_DESTINATION = "analyticsDestination";
	private static final String API_ANALYTICS = "/api/analytics";
	private static final String DATASET = "/dataset";
	private static final String SYNC = "/sync";
	private static final String OUTLIERS = "/outliers";
	private static final String API_ANALYTICS_DATASET = API_ANALYTICS + DATASET;
	private static final String API_ANALYTICS_OUTLIERS_SYNC = API_ANALYTICS + OUTLIERS + SYNC;
	private static final String ANALYTICS_SCHEMA = "MDC_DATA1";
	private static final Integer DATASET_ID = 1;

	private static final int COPY_CONTENT_BUFFER_SIZE = 1024;
	private static final Logger LOGGER = LoggerFactory.getLogger(Analytics.class);

	private DataSource ds;
	private EntityManagerFactory emf;
	DataManager dataManager;

	public Analytics() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = Utils.encodeText(request.getParameter("action"));

		if (action == null) {
			doGetOutliers(response);
			return;
		}

		if (action.equals("controlAnomalies")) {
			controlOutliers(response);
		}
	}

	private void controlOutliers(HttpServletResponse response) throws IOException {
		HttpClient httpClient = null;
		dataManager = new DataManager(emf);

		try {
			String outliers = getOutliers(httpClient);

			if (outliers == null) {
				return;
			}

			JsonParser parser = new JsonParser();
			JsonObject outliersJson = parser.parse(outliers).getAsJsonObject();
			JsonArray outliersJsonElements = outliersJson.get("outliers").getAsJsonArray();
			ArrayList<String> controledAnomaliesVehicleIds = new ArrayList<String>();

			for (int i = 0; i < outliersJsonElements.size(); i++) {
				JsonObject outlier = outliersJsonElements.get(i).getAsJsonObject();
				JsonObject dataPoint = outlier.get("dataPoint").getAsJsonObject();
				String outlierVehicleId = dataPoint.get("VEHICLEID").getAsString();
				List<Vehicle> vehicles = dataManager.getListOfVehicles();
				for (Vehicle vehicle : vehicles) {
					String vehicleId = vehicle.getId();
					if (!vehicleId.equals(outlierVehicleId)) {
						continue;
					}
					dataManager.setVehicleEcoState(vehicleId, true);
					controledAnomaliesVehicleIds.add(outlierVehicleId);
				}
			}

			int controledAnomaliesSize = controledAnomaliesVehicleIds.size();
			String responseJson = "{vehicleIds=[";
			for (int i = 0; i < controledAnomaliesSize; i++) {
				responseJson += controledAnomaliesVehicleIds.get(i);
				if (controledAnomaliesSize > 1 && i < controledAnomaliesSize - 1) {
					responseJson += ", ";
				}
			}
			responseJson += "], count=" + controledAnomaliesSize + "}";
			response.getWriter().println(responseJson);

		} catch (NamingException e) {
			String errorMessage = "Lookup of destination failed with reason: " + e.getMessage() + ". See " + "logs for details. Hint: Make sure to have the destination " + ANALYTICS_DESTINATION
					+ " configured.";
			LOGGER.error("Lookup of destination failed", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
		} catch (Exception e) {
			String errorMessage = "Connectivity operation failed with reason: " + e.getMessage() + ". See " + "logs for details. Hint: Make sure to have an HTTP proxy configured in your "
					+ "local Eclipse environment in case your environment uses " + "an HTTP proxy for the outbound Internet " + "communication.";
			LOGGER.error("Connectivity operation failed", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
		} finally {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	private void doGetOutliers(HttpServletResponse response) throws IOException {
		HttpClient httpClient = null;

		try {
			String outliers = getOutliers(httpClient);

			if (outliers != null) {
				response.getWriter().println(outliers);
			}
		} catch (NamingException e) {
			String errorMessage = "Lookup of destination failed with reason: " + e.getMessage() + ". See " + "logs for details. Hint: Make sure to have the destination " + ANALYTICS_DESTINATION
					+ " configured.";
			LOGGER.error("Lookup of destination failed", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
		} catch (Exception e) {
			String errorMessage = "Connectivity operation failed with reason: " + e.getMessage() + ". See " + "logs for details. Hint: Make sure to have an HTTP proxy configured in your "
					+ "local Eclipse environment in case your environment uses " + "an HTTP proxy for the outbound Internet " + "communication.";
			LOGGER.error("Connectivity operation failed", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
		} finally {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
	}

	private String getOutliers(HttpClient httpClient) throws NamingException, DestinationException, URISyntaxException, ClientProtocolException, IOException, ServletException {
		Context ctx = new InitialContext();
		String destinationName = ANALYTICS_DESTINATION;
		HttpDestination destination = (HttpDestination) ctx.lookup("java:comp/env/" + destinationName);
		httpClient = destination.createHttpClient();

		URI uri = destination.getURI();
		String datasetUri = uri.getScheme() + "://" + uri.getHost() + uri.getPath() + API_ANALYTICS_DATASET + "/" + DATASET_ID.toString();
		String outliersSyncUriString = uri.getScheme() + "://" + uri.getHost() + uri.getPath() + API_ANALYTICS_OUTLIERS_SYNC;
		URI outliersSyncUri = new URI(outliersSyncUriString);

		HttpPost httpPost = new HttpPost(outliersSyncUri);

		Gson gson = new Gson();
		StringEntity postingString = new StringEntity(gson.toJson(new OutliersConfig()));
		httpPost.setEntity(postingString);
		httpPost.setHeader("Content-type", "application/json");

		HttpResponse httpResponse = httpClient.execute(httpPost);
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		if (statusCode != HTTP_OK) {
			throw new ServletException("Expected response status code is 200 but it is " + statusCode + " . " + " Uri2= " + outliersSyncUri.toURL().toString());
		}

		// Copy content from the incoming response to the outgoing response
		HttpEntity entity = httpResponse.getEntity();
		String inputResponseJson = null;

		if (entity != null) {
			// copyInResponseToOutResponse(response, httpPost, entity);
			inputResponseJson = EntityUtils.toString(httpResponse.getEntity());
		}

		return inputResponseJson;
	}

	private void copyInResponseToOutResponse(HttpServletResponse response, HttpPost httpPost, HttpEntity entity) throws IOException {
		InputStream instream = entity.getContent();
		try {
			byte[] buffer = new byte[COPY_CONTENT_BUFFER_SIZE];
			int len;
			while ((len = instream.read(buffer)) != -1) {
				response.getOutputStream().write(buffer, 0, len);
			}
		} catch (IOException e) {
			// In case of an IOException the connection will be released
			// back to the connection manager automatically
			throw e;
		} catch (RuntimeException e) {
			// In case of an unexpected exception you may want to abort
			// the HTTP request in order to shut down the underlying
			// connection immediately.
			httpPost.abort();
			throw e;
		} finally {
			// Closing the input stream will trigger connection release
			try {
				instream.close();
			} catch (Exception e) {
				// Ignore
			}
		}
	}

	class OutliersConfig {
		// {
		// "datasetID": 1,
		// "targetColumn" : "CO2EMISSIONSMASS",
		// "weightVariable" : "AIRMASS",
		// "skippedVariables" : ["ID"), "STOREDAT"],
		// "numberOfReasons" : 1,
		// "numberOfOutliers" : 10
		// }

		Integer datasetID = new Integer(DATASET_ID);
		String targetColumn = "CO2EMISSIONSMASS";
		// String weightVariable = "AIRMASS";
		String[] skippedVariables = { "ID", "STOREDAT", "MAP", "AFR", "TEMP" };
		// Integer numberOfReasons = 1;
		Integer numberOfOutliers = 10;

		public Integer getDatasetID() {
			return datasetID;
		}

		public void setDatasetID(Integer datasetID) {
			this.datasetID = datasetID;
		}

		public String getTargetColumn() {
			return targetColumn;
		}

		public void setTargetColumn(String targetColumn) {
			this.targetColumn = targetColumn;
		}

		public String[] getSkippedVariables() {
			return skippedVariables;
		}

		public void setSkippedVariables(String[] skippedVariables) {
			this.skippedVariables = skippedVariables;
		}

		public Integer getNumberOfOutliers() {
			return numberOfOutliers;
		}

		public void setNumberOfOutliers(Integer numberOfOutliers) {
			this.numberOfOutliers = numberOfOutliers;
		}

		// public String getWeightVariable() {
		// return weightVariable;
		// }
		//
		// public void setWeightVariable(String weightVariable) {
		// this.weightVariable = weightVariable;
		// }
		//
		// public Integer getNumberOfReasons() {
		// return numberOfReasons;
		// }
		//
		// public void setNumberOfReasons(Integer numberOfReasons) {
		// this.numberOfReasons = numberOfReasons;
		// }
	}

	@Override
	public void init() throws ServletException {

		try {
			InitialContext ctx = new InitialContext();
			ds = (DataSource) ctx.lookup("java:comp/env/jdbc/DefaultDB");

			Map properties = new HashMap();
			properties.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, ds);
			emf = Persistence.createEntityManagerFactory("vehicle_monitor_controller.backend.persistence_unit", properties);
		} catch (NamingException e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void destroy() {
		emf.close();
	}

}
