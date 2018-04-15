package org.ttsokov.vehicle_monitor_controller.backend.servlets;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

import java.net.URI;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ttsokov.vehicle_monitor_controller.backend.data_manager.DataManager;
import org.ttsokov.vehicle_monitor_controller.backend.enums.SensorTypes;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.GetSensorByIdException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.GetSensorsByVehicleIdException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.GetVehicleByIdException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.SetAirMassException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.SetCO2EmissionsMassException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.SetFuelMassException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.SetVehicleEcoStateException;
import org.ttsokov.vehicle_monitor_controller.backend.model.Measurement;
import org.ttsokov.vehicle_monitor_controller.backend.model.Sensor;
import org.ttsokov.vehicle_monitor_controller.backend.model.Vehicle;
import org.ttsokov.vehicle_monitor_controller.utils.Utils;

import com.google.gson.JsonObject;
import com.sap.core.connectivity.api.http.HttpDestination;

public class AirFuelEmissionsHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(AirFuelEmissionsHandler.class);
	private static final String MDC_DESTINATION = "mdcDestination";
	private static final String POST_SENSOR_DATA_URL = "/postsensordata.xsjs";

	private DataManager dataManager;
	private static AirFuelEmissionsHandler airFuelEmissionsHandler;

	private AirFuelEmissionsHandler(DataManager dataManager) {
		this.dataManager = dataManager;
	}

	public static AirFuelEmissionsHandler getInstance(DataManager dataManager) {
		if (airFuelEmissionsHandler == null) {
			airFuelEmissionsHandler = new AirFuelEmissionsHandler(dataManager);
		}
		return airFuelEmissionsHandler;
	}

	void handleAirFuelEmissionsRegulation(HttpServletRequest request, Measurement lastMeasurement) throws GetVehicleByIdException, GetSensorsByVehicleIdException, SetAirMassException,
			GetSensorByIdException, SetFuelMassException, SetCO2EmissionsMassException, SetVehicleEcoStateException {
		String sensorId = Utils.encodeText(request.getParameter("sensorId"));
		String vehicleId;

		if (sensorId != null && sensorId.length() > 0) {
			Sensor sensor = dataManager.getSensorById(sensorId);
			vehicleId = sensor.getVehicleId();
		} else {
			vehicleId = Utils.encodeText(request.getParameter("id"));
		}

		Vehicle vehicle = dataManager.getVehicleById(vehicleId);
		Double airMassMg = getAirMassMg(vehicle, lastMeasurement);
		Double fuelMassMg = getFuelMassMg(airMassMg, vehicle, lastMeasurement);
		Double co2EmissionsMassMg = getCo2EmissionsMassMg(fuelMassMg);
		dataManager.setAirMass(vehicleId, airMassMg);
		dataManager.setFuelMass(vehicleId, fuelMassMg);
		dataManager.setCO2EmissionsMass(vehicleId, co2EmissionsMassMg);
		updateDataInAnalyticsSchema(vehicle, lastMeasurement, airMassMg, fuelMassMg, co2EmissionsMassMg);
		handleEcoState(vehicle, co2EmissionsMassMg);
	}

	private void updateDataInAnalyticsSchema(Vehicle vehicle, Measurement lastMeasurement, Double airMassMg, Double fuelMassMg, Double co2EmissionsMassMg) throws GetSensorsByVehicleIdException {
		HttpClient httpClient = null;

		try {
			Context ctx = new InitialContext();
			HttpDestination destination = (HttpDestination) ctx.lookup("java:comp/env/" + MDC_DESTINATION);
			httpClient = destination.createHttpClient();

			// VEHICLEID, ENGINECAPACITY, STOREDAT, MAP, AFR, TEMP, AIRMASS,
			// FUELMASS, CO2EMISSIONSMASS
			String vehicleId = vehicle.getId();
			Double engineCapacity = vehicle.getEngineCapacity();
			Double map = getLastMeasurementValue(SensorTypes.MAP, vehicle, lastMeasurement);
			Double afr = getLastMeasurementValue(SensorTypes.AFR, vehicle, lastMeasurement);
			Double temp = getLastMeasurementValue(SensorTypes.TEMP, vehicle, lastMeasurement);

			JsonObject json = new JsonObject();
			json.addProperty("vehicleId", vehicleId);
			json.addProperty("engineCapacity", engineCapacity);
			json.addProperty("map", map);
			json.addProperty("afr", afr);
			json.addProperty("temp", temp);
			json.addProperty("airMass", airMassMg);
			json.addProperty("fuelMass", fuelMassMg);
			json.addProperty("co2EmissionsMass", co2EmissionsMassMg);

			URI uri = destination.getURI();
			String postSensorDataUrl = uri.getScheme() + "://" + uri.getHost() + uri.getPath() + POST_SENSOR_DATA_URL;
			URI postSensorDataUri = new URI(postSensorDataUrl);
			HttpPost httpPost = new HttpPost(postSensorDataUri);

			HttpEntity entity = new StringEntity(json.toString());
			httpPost.setEntity(entity);

			HttpResponse httpResponse = httpClient.execute(httpPost);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode != HTTP_OK && statusCode != HTTP_CREATED) {
				throw new ServletException("Expected response status code is 200|201 but it is " + statusCode + " . " + " Uri2= " + postSensorDataUri.toURL().toString());
			}

			// Copy content from the incoming response to the outgoing response
			HttpEntity responseEntity = httpResponse.getEntity();
			if (responseEntity != null) {
				String inputResponseJson = EntityUtils.toString(httpResponse.getEntity());
			}
		} catch (NamingException e) {
			String errorMessage = "Lookup of destination failed with reason: " + e.getMessage() + ". See " + "logs for details. Hint: Make sure to have the destination " + MDC_DESTINATION
					+ " configured.";
			LOGGER.error("Lookup of destination failed", e);
			// response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
			// errorMessage);
		} catch (Exception e) {
			String errorMessage = "Connectivity operation failed with reason: " + e.getMessage() + ". See " + "logs for details. Hint: Make sure to have an HTTP proxy configured in your "
					+ "local Eclipse environment in case your environment uses " + "an HTTP proxy for the outbound Internet " + "communication.";
			LOGGER.error("Connectivity operation failed", e);
			// response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
			// errorMessage);
		} finally {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
	}

	private Double getAirMassMg(Vehicle vehicle, Measurement lastMeasurement) throws GetSensorsByVehicleIdException {
		Double SPECIFIC_GAS_CONSTANT_FOR_AIR = 8.314; // J·K−1·mol−1 287.058
														// Rspec for dry air
		Double AIR_MOLAR_MASS = 28.97; // g/mol
		Double ENGINE_VOLUMETRIC_EFFICIENCY = 0.05;

		Double engineCapacityCm3 = vehicle.getEngineCapacity();
		Double engineCapacityM3 = (engineCapacityCm3 / 1000000);
		Double lastMAPBars = getLastMeasurementValue(SensorTypes.MAP, vehicle, lastMeasurement); // Boost
																									// value
																									// (without
																									// atmosphere
																									// press)

		Double lastMAPWithAtmPressureBars = lastMAPBars + 1.0;
		if (lastMAPWithAtmPressureBars < 0) {
			lastMAPWithAtmPressureBars = 0.3; // Idle air pressure
		}
		Double lastMAPWithAtmPressurePascals = (lastMAPWithAtmPressureBars * 100000);
		Double lastAirTempKelvins = getAirTempKelvins(vehicle, lastMeasurement);
		// p*V=n*Rspec*T
		// n=m/M
		Double airAmountMols = (lastMAPWithAtmPressurePascals * engineCapacityM3) / (SPECIFIC_GAS_CONSTANT_FOR_AIR * lastAirTempKelvins);
		Double airMassGrams = (airAmountMols * AIR_MOLAR_MASS);
		Double airMassMg = (airMassGrams * 1000);
		airMassMg = (airMassMg * ENGINE_VOLUMETRIC_EFFICIENCY);
		airMassMg = Utils.round(airMassMg, 2);
		return airMassMg;
	}

	private Double getLastMeasurementValue(SensorTypes sensorType, Vehicle vehicle, Measurement lastMeasurement) throws GetSensorsByVehicleIdException {
		String unit = lastMeasurement.getUnit();

		if (unit.equals(sensorType.getUnit())) {
			return lastMeasurement.getValue();
		}

		List<Sensor> sensors = dataManager.getSensorsByVehicleId(vehicle.getId());
		for (Sensor sensor : sensors) {
			if (sensor.getType().equalsIgnoreCase(sensorType.getType())) {
				Measurement lastPersistedMeasurement = dataManager.getLastSensorMeasurement(sensor.getId());
				if (lastPersistedMeasurement != null) {
					return lastPersistedMeasurement.getValue();
				}
			}
		}

		return 1.0;
	}

	private Double getAirTempKelvins(Vehicle vehicle, Measurement lastMeasurement) throws GetSensorsByVehicleIdException {
		// Temp
		Double lastAirTempCelsius = getLastMeasurementValue(SensorTypes.TEMP, vehicle, lastMeasurement);
		Double lastAirTempKelvins = (lastAirTempCelsius + 274.15);
		lastAirTempKelvins = Utils.round(lastAirTempKelvins, 2);
		return lastAirTempKelvins;
	}

	private Double getFuelMassMg(Double airMassMg, Vehicle vehicle, Measurement lastMeasurement) throws GetSensorsByVehicleIdException {
		Double fuelMass;
		Double lastAFR = getLastMeasurementValue(SensorTypes.AFR, vehicle, lastMeasurement);
		// AFR = massAir/massFuel
		fuelMass = (airMassMg / lastAFR);
		fuelMass = Utils.round(fuelMass, 2);
		return fuelMass;
	}

	private Double getCo2EmissionsMassMg(Double fuelMassMg) {
		Double CO2_MASS_PER_FUEL_MASS = 1.73;// kg

		Double co2EmmissionsMassMg = (fuelMassMg * CO2_MASS_PER_FUEL_MASS);
		co2EmmissionsMassMg = Utils.round(co2EmmissionsMassMg, 2);
		return co2EmmissionsMassMg;
	}

	private void handleEcoState(Vehicle vehicle, Double currentCO2EmissionsMg) throws SetVehicleEcoStateException {
		boolean isAutoRegulationActuatorState = vehicle.isAutoRegulationActuatorState();

		if (!isAutoRegulationActuatorState) {
			return;
		}

		handleEcoStateBySlider(vehicle, currentCO2EmissionsMg);
	}

	private void handleEcoStateBySlider(Vehicle vehicle, Double currentCO2EmissionsMg) throws SetVehicleEcoStateException {
		int co2EmissionsLimit = vehicle.getCo2EmissionsLimit();
		if (currentCO2EmissionsMg >= co2EmissionsLimit) {
			dataManager.setVehicleEcoState(vehicle.getId(), true);
		} else {
			dataManager.setVehicleEcoState(vehicle.getId(), false);
		}
	}
}
