package org.ttsokov.vehicle_monitor_controller.backend.servlets;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.ttsokov.vehicle_monitor_controller.backend.data_manager.DataManager;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.AddMeasurementException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.ExtractMeasurementException;
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

public class AddMeasurement extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private EntityManagerFactory emf;
	private DataSource ds;
	private DataManager dataManager;

	public AddMeasurement() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		dataManager = new DataManager(emf);
		boolean isEcoState = false;
		AirFuelEmissionsHandler airFuelEmissionsHandler = AirFuelEmissionsHandler.getInstance(dataManager);

		try {
			Measurement measurement = extractMeasurement(request, response);
			dataManager.addMeasurement(measurement);
			airFuelEmissionsHandler.handleAirFuelEmissionsRegulation(request, measurement);
			isEcoState = isEcoState(request);
		} catch (AddMeasurementException | ExtractMeasurementException | GetSensorByIdException | GetVehicleByIdException e) {
			response.getWriter().println(e.getMessage());
			response.setStatus(javax.servlet.http.HttpServletResponse.SC_FORBIDDEN);
			return;
		} catch (SetAirMassException e) {
		} catch (SetFuelMassException e) {
		} catch (SetCO2EmissionsMassException e) {
		} catch (SetVehicleEcoStateException e) {
		} catch (GetSensorsByVehicleIdException e) {
		}

		response.getWriter().println("isEcoState=" + isEcoState);
	}

	private boolean isEcoState(HttpServletRequest request) throws IOException, GetSensorByIdException, GetVehicleByIdException {
		String sensorId = Utils.encodeText(request.getParameter("sensorId"));
		String vehicleId;

		Sensor sensor;
		sensor = dataManager.getSensorById(sensorId);
		vehicleId = sensor.getVehicleId();
		Vehicle vehicle = dataManager.getVehicleById(vehicleId);

		return vehicle.isEcoState();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		dataManager = new DataManager(emf);
		try {
			Measurement measurement = extractMeasurement(request, response);
			dataManager.addMeasurement(measurement);

		} catch (AddMeasurementException | ExtractMeasurementException | GetSensorByIdException e) {
			response.getWriter().println(e.getMessage());
			response.setStatus(javax.servlet.http.HttpServletResponse.SC_FORBIDDEN);
		}
	}

	private Measurement extractMeasurement(HttpServletRequest request, HttpServletResponse response) throws IOException, ExtractMeasurementException, GetSensorByIdException {
		Measurement measurement = new Measurement();
		String sensorId = Utils.encodeText(request.getParameter("sensorId"));

		if (sensorId == null) {
			throw new ExtractMeasurementException();
		}

		Sensor sensor = dataManager.getSensorById(sensorId);

		measurement.setSensorId(sensorId);
		measurement.setUnit(Utils.encodeText(request.getParameter("unit")));

		String sensorValue = Utils.encodeText(request.getParameter("sensorValue"));
		String sensorValueMultiplier = Utils.encodeText(request.getParameter("sensorValueMultiplier"));
		String sensorValueCalibration = Utils.encodeText(request.getParameter("sensorValueCalibration"));

		if (sensorValueCalibration != null && sensorValueCalibration.length() > 0 && sensorValue != null && sensorValueMultiplier != null && sensorValueMultiplier.length() > 0
				&& sensorValue.length() > 0) {
			measurement.setStoredAt(new Timestamp(new Date().getTime()));

			Double valueDouble = Double.parseDouble(sensorValue);
			Double multiplierDouble = Double.parseDouble(sensorValueMultiplier);
			Double valueGap = Double.parseDouble(sensorValueCalibration);
			Double value = (valueDouble * multiplierDouble) + valueGap;
			measurement.setValue(value);
		}
		return measurement;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
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
