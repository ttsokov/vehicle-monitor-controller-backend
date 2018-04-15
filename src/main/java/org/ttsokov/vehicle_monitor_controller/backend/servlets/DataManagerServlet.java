package org.ttsokov.vehicle_monitor_controller.backend.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.AddSensorException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.AddVehicleException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.DeleteMeasurementException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.DeleteSensorException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.DeleteVehicleException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.ExtractSensorException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.GetMeasurementByIdException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.GetSensorByIdException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.GetSensorsByVehicleIdException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.GetSensorsException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.GetVehicleByIdException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.GetVehiclesException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.SetAirMassException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.SetCO2EmissionsLimitException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.SetCO2EmissionsMassException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.SetFuelMassException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.SetVehicleAutoRegulationActuatorStateException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.SetVehicleEcoStateException;
import org.ttsokov.vehicle_monitor_controller.backend.model.Measurement;
import org.ttsokov.vehicle_monitor_controller.backend.model.NullMeasurement;
import org.ttsokov.vehicle_monitor_controller.backend.model.Sensor;
import org.ttsokov.vehicle_monitor_controller.backend.model.Vehicle;
import org.ttsokov.vehicle_monitor_controller.utils.Utils;

import com.google.gson.Gson;

public class DataManagerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final int MAX_NUMBER_SENSOR_READINGS = 10;
	private DataSource ds;
	private EntityManagerFactory emf;
	DataManager dataManager;
	private AirFuelEmissionsHandler airFuelEmissionsHandler;

	public DataManagerServlet() {
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		dataManager = new DataManager(emf);
		String action = Utils.encodeText(request.getParameter("action"));
		airFuelEmissionsHandler = AirFuelEmissionsHandler.getInstance(dataManager);

		if (action == null) {
			getAllVehiclesData(request, response);
			return;
		}

		switch (action) {
		case "getAllVehiclesData":
			getAllVehiclesData(request, response);
			break;

		case "getAllData":
			getAllData(request, response);
			break;

		case "addVehicle":
			addVehicle(request, response);
			break;

		case "addSensor":
			addSensor(request, response);
			break;

		case "deleteVehicle":
			deleteVehicle(request, response);
			break;

		case "deleteSensor":
			deleteSensor(request, response);
			break;

		case "deleteMeasurement":
			deleteMeasurement(request, response);
			break;

		case "setVehicleEcoState":
			setVehicleEcoState(request, response);
			break;

		case "setVehicleAutoRegulationActuatorState":
			setVehicleAutoRegulationActuatorState(request, response);
			break;

		case "setCO2EmissionsLimit":
			setCO2EmissionsLimit(request, response);
			break;

		default:
			getAllVehiclesData(request, response);
			break;
		}
	}

	private void getAllVehiclesData(HttpServletRequest request, HttpServletResponse response) throws IOException {
		List<Vehicle> vehicles;

		try {
			vehicles = dataManager.getListOfVehicles();
			for (int i = 0; i < vehicles.size(); i++) {
				Vehicle vehicle = vehicles.get(i);
				List<Sensor> vehicleSensors = dataManager.getSensorsByVehicleId(vehicle.getId());
				for (int j = 0; j < vehicleSensors.size(); j++) {
					Sensor sensor = vehicleSensors.get(j);
					List<Measurement> sensorMeasurements = dataManager.getLastSensorMeasurements(sensor.getId(), MAX_NUMBER_SENSOR_READINGS);
					sensor.setMeasurements(sensorMeasurements);
					Measurement sensorLastMeasurement = dataManager.getLastSensorMeasurement(sensor.getId());
					sensor.setLastMeasurement(sensorLastMeasurement);
					vehicleSensors.set(j, sensor);
				}
				vehicle.setSensors(vehicleSensors);
				vehicles.set(i, vehicle);
			}

			outputListToJson(response, vehicles, "vehiclesData", false);
		} catch (GetSensorsByVehicleIdException | GetVehiclesException e) {
			response.getWriter().println(e.getMessage());
			response.setStatus(javax.servlet.http.HttpServletResponse.SC_FORBIDDEN);
		}
	}

	private void getAllData(HttpServletRequest request, HttpServletResponse response) throws IOException {
		List<Vehicle> vehicles = new ArrayList<Vehicle>();
		try {
			vehicles = dataManager.getListOfVehicles();
			List<Sensor> vehicleSensors = new ArrayList<Sensor>();
			vehicleSensors = dataManager.getListOfSensors();
			List<Measurement> sensorMeasurements = dataManager.getAllSensorMeasurements();

			outputListToJson(response, vehicles, "Vehicles", true);
			outputListToJson(response, vehicleSensors, "Sensors", true);
			outputListToJson(response, sensorMeasurements, "Measurements", true);

			getAllVehiclesData(request, response);
		} catch (GetVehiclesException | GetSensorsException e) {
			response.getWriter().println(e.getMessage());
			response.setStatus(javax.servlet.http.HttpServletResponse.SC_FORBIDDEN);
		}
	}

	private void outputListToJson(HttpServletResponse response, List list, String key, boolean existsEndComma) {
		Gson gson = new Gson();
		try {
			response.getWriter().println("{\"" + key + "\": [");
			for (int i = 0; i < list.size(); i++) {
				response.getWriter().println(gson.toJson(list.get(i)));

				if (i != list.size() - 1) {
					response.getWriter().println(",");
				}
			}
			response.getWriter().println("]}" + ((existsEndComma == true) ? "," : ""));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addVehicle(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Vehicle vehicle = extractVehicleData(request, response);
		try {
			dataManager.addVehicle(vehicle);
		} catch (AddVehicleException e) {
			response.getWriter().println(e.getMessage());
			response.setStatus(javax.servlet.http.HttpServletResponse.SC_FORBIDDEN);
		}
	}

	private Vehicle extractVehicleData(HttpServletRequest request, HttpServletResponse response) {
		Vehicle vehicle = new Vehicle();
		String idString = Utils.encodeText(request.getParameter("id"));

		if (idString != null && idString.length() > 0) {
			vehicle.setId(idString);
		}

		vehicle.setModel(Utils.encodeText(request.getParameter("model")));
		vehicle.setEngineCapacity(Double.parseDouble(Utils.encodeText(request.getParameter("engine"))));

		return vehicle;
	}

	private void addSensor(HttpServletRequest request, HttpServletResponse response) throws IOException {

		try {
			Sensor sensor = extractSensorData(request, response);
			dataManager.addSensor(sensor);
		} catch (AddSensorException | ExtractSensorException | GetVehicleByIdException e) {
			response.getWriter().println(e.getMessage());
			response.setStatus(javax.servlet.http.HttpServletResponse.SC_FORBIDDEN);
		}
	}

	private Sensor extractSensorData(HttpServletRequest request, HttpServletResponse response) throws IOException, ExtractSensorException, GetVehicleByIdException {
		String idString = Utils.encodeText(request.getParameter("id"));

		if (idString == null || idString.length() <= 0) {
			throw new ExtractSensorException();
		}

		String vehicleId = Utils.encodeText(request.getParameter("vehicleId"));

		dataManager.getVehicleById(vehicleId);

		Sensor sensor = new Sensor();
		sensor.setId(idString);
		sensor.setType(Utils.encodeText(request.getParameter("type")));
		sensor.setDescription(Utils.encodeText(request.getParameter("description")));
		sensor.setVehicleId(vehicleId);

		return sensor;
	}

	private void deleteVehicle(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String idString = Utils.encodeText(request.getParameter("id"));

		if (idString != null && idString.length() > 0) {
			try {
				Vehicle vehicle = dataManager.getVehicleById(idString);
				dataManager.deleteVehicle(vehicle);
			} catch (DeleteVehicleException | GetVehicleByIdException e) {
				response.getWriter().println(e.getMessage());
				response.setStatus(javax.servlet.http.HttpServletResponse.SC_FORBIDDEN);
			}
		}
	}

	private void deleteSensor(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String idString = Utils.encodeText(request.getParameter("id"));

		if (idString != null && idString.length() > 0) {
			try {
				Sensor sensor = dataManager.getSensorById(idString);
				dataManager.deleteSensor(sensor);
			} catch (DeleteSensorException | GetSensorByIdException e) {
				response.getWriter().println(e.getMessage());
				response.setStatus(javax.servlet.http.HttpServletResponse.SC_FORBIDDEN);
			}
		}
	}

	private void deleteMeasurement(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String idString = Utils.encodeText(request.getParameter("id"));

		if (idString != null && idString.length() > 0) {

			try {
				Measurement measurement = dataManager.getMeasurementById(Long.parseLong(idString));
				dataManager.deleteMeasurement(measurement);
			} catch (DeleteMeasurementException | GetMeasurementByIdException e) {
				response.getWriter().println(e.getMessage());
				response.setStatus(javax.servlet.http.HttpServletResponse.SC_FORBIDDEN);
			}
		}
	}

	private void setVehicleEcoState(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String vehicleIdString = Utils.encodeText(request.getParameter("id"));

		if (vehicleIdString != null && vehicleIdString.length() > 0) {
			String isEcoStateString = Utils.encodeText(request.getParameter("isEcoState"));

			try {
				dataManager.setVehicleEcoState(vehicleIdString, Boolean.parseBoolean(isEcoStateString));
			} catch (SetVehicleEcoStateException e) {
				response.getWriter().println(e.getMessage());
				response.setStatus(javax.servlet.http.HttpServletResponse.SC_FORBIDDEN);
			}
		}
	}

	private void setVehicleAutoRegulationActuatorState(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String vehicleIdString = Utils.encodeText(request.getParameter("id"));

		if (vehicleIdString != null && vehicleIdString.length() > 0) {
			String isAutoRegulationString = Utils.encodeText(request.getParameter("isAutoRegulation"));

			try {
				dataManager.setVehicleAutoRegulationActuatorState(vehicleIdString, Boolean.parseBoolean(isAutoRegulationString));
				airFuelEmissionsHandler.handleAirFuelEmissionsRegulation(request, new NullMeasurement());
			} catch (SetVehicleAutoRegulationActuatorStateException e) {
				response.getWriter().println(e.getMessage());
				response.setStatus(javax.servlet.http.HttpServletResponse.SC_FORBIDDEN);
			} catch (GetSensorByIdException | GetVehicleByIdException | SetAirMassException | SetFuelMassException | SetCO2EmissionsMassException | SetVehicleEcoStateException
					| GetSensorsByVehicleIdException e) {
			}
		}
	}

	private void setCO2EmissionsLimit(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String vehicleIdString = Utils.encodeText(request.getParameter("id"));

		if (vehicleIdString != null && vehicleIdString.length() > 0) {
			String co2EmissionsLimitString = Utils.encodeText(request.getParameter("co2EmissionsLimit"));

			try {
				dataManager.setCO2EmissionsLimit(vehicleIdString, Integer.parseInt(co2EmissionsLimitString));
				airFuelEmissionsHandler.handleAirFuelEmissionsRegulation(request, new NullMeasurement());
			} catch (NumberFormatException | SetCO2EmissionsLimitException e) {
				response.getWriter().println(e.getMessage());
				response.setStatus(javax.servlet.http.HttpServletResponse.SC_FORBIDDEN);
			} catch (GetSensorByIdException | GetVehicleByIdException | SetAirMassException | SetFuelMassException | SetCO2EmissionsMassException | SetVehicleEcoStateException
					| GetSensorsByVehicleIdException e) {
			}
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
