package org.ttsokov.vehicle_monitor_controller.backend.data_manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.ttsokov.vehicle_monitor_controller.backend.exceptions.AddMeasurementException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.AddSensorException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.AddVehicleException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.DeleteMeasurementException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.DeleteSensorException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.DeleteVehicleException;
import org.ttsokov.vehicle_monitor_controller.backend.exceptions.GetAllMeasurementsBySensorIdException;
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
import org.ttsokov.vehicle_monitor_controller.backend.model.Sensor;
import org.ttsokov.vehicle_monitor_controller.backend.model.Vehicle;

public class DataManager {

	private EntityManagerFactory emf;

	public DataManager(EntityManagerFactory emf) {
		this.emf = emf;
	}

	public boolean addMeasurement(Measurement measurement) throws AddMeasurementException {
		boolean result = false;

		EntityManager em = emf.createEntityManager();
		try {
			if (measurement != null && measurement.getValue() != null) {
				em.getTransaction().begin();
				em.persist(measurement);
				em.getTransaction().commit();
			}
		} catch (Exception e) {
			throw new AddMeasurementException(e);
		} finally {
			em.close();
		}

		return result;
	}

	public boolean deleteMeasurement(Measurement measurement) throws DeleteMeasurementException {
		boolean result = false;

		if (measurement == null) {
			return result;
		}

		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			measurement = em.merge(measurement);
			em.remove(measurement);
			em.getTransaction().commit();
			result = true;
		} catch (Exception e) {
			throw new DeleteMeasurementException(e);
		}
		em.close();

		return result;
	}

	public Measurement getMeasurementById(long measurementId) throws GetMeasurementByIdException {
		Measurement result = null;

		EntityManager em = emf.createEntityManager();
		try {
			Query q = em.createNamedQuery("GetMeasurementById");
			q.setParameter("paramMeasurementId", measurementId);
			result = (Measurement) q.getSingleResult();
		} catch (Exception e) {
			throw new GetMeasurementByIdException(e);
		}

		em.close();
		return result;
	}

	public boolean addSensor(Sensor sensor) throws AddSensorException {
		boolean result = false;

		if (sensor == null) {
			return result;
		}

		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(sensor);
			em.getTransaction().commit();
			result = true;
		} catch (Exception e) {
			throw new AddSensorException(e);
		}
		em.close();

		return result;
	}

	public boolean deleteSensor(Sensor sensor) throws DeleteSensorException {
		boolean result = false;

		if (sensor == null) {
			return result;
		}

		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			sensor = em.merge(sensor);

			Collection<Measurement> measurements = this.getAllMeasurementsBySensorId(sensor.getId());

			for (Measurement measurement : measurements) {
				this.deleteMeasurement(measurement);
			}

			em.remove(sensor);
			em.getTransaction().commit();
			result = true;
		} catch (Exception e) {
			throw new DeleteSensorException(e);
		}
		em.close();

		return result;
	}

	public boolean addVehicle(Vehicle vehicle) throws AddVehicleException {
		boolean result = false;

		if (vehicle == null) {
			return result;
		}

		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(vehicle);
			em.getTransaction().commit();
			result = true;
		} catch (Exception e) {
			throw new AddVehicleException(e);
		}
		em.close();

		return result;
	}

	public boolean deleteVehicle(Vehicle vehicle) throws DeleteVehicleException {
		boolean result = false;

		if (vehicle == null) {
			return result;
		}

		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			vehicle = em.merge(vehicle);

			Collection<Sensor> sensors = this.getSensorsByVehicleId(vehicle.getId());

			for (Sensor sensor : sensors) {
				this.deleteSensor(sensor);
			}

			em.remove(vehicle);
			em.getTransaction().commit();
			result = true;
		} catch (Exception e) {
			throw new DeleteVehicleException(e);
		}
		em.close();

		return result;
	}

	public List<Sensor> getSensorsByVehicleId(String vehicleId) throws GetSensorsByVehicleIdException {
		List<Sensor> result = null;

		EntityManager em = emf.createEntityManager();
		try {
			Query q = em.createNamedQuery("GetSensorsByVehicleId");
			q.setParameter("paramVehicleId", vehicleId);
			result = q.getResultList();
		} catch (Exception e) {
			throw new GetSensorsByVehicleIdException(e);
		}
		em.close();

		return result;
	}

	public List<Measurement> getAllMeasurementsBySensorId(String sensorId) throws GetAllMeasurementsBySensorIdException {
		List<Measurement> result = null;

		EntityManager em = emf.createEntityManager();
		try {
			Query q = em.createNamedQuery("GetAllMeasurementsBySensorId");
			// q.setMaxResults(500);
			q.setParameter("paramSensorId", sensorId);
			result = q.getResultList();
		} catch (Exception e) {
			throw new GetAllMeasurementsBySensorIdException(e);
		}
		em.close();

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Measurement> getLastSensorMeasurements(String sensorId, int numberOfReadings) {
		List<Measurement> result = null;

		EntityManager em = emf.createEntityManager();
		try {

			Query q = em.createNamedQuery("GetLastSensorMeasurements");
			q.setParameter("paramSensorId", sensorId);
			// To not affect performance we just retrieve the first 20 result
			// sets
			q.setMaxResults(numberOfReadings);
			result = q.getResultList();

			Collections.sort(result, new Comparator<Measurement>() {
				public int compare(Measurement m1, Measurement m2) {
					return m1.getStoredAt().compareTo(m2.getStoredAt());
				}
			});

		} catch (Exception e) {
		}

		em.close();
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Measurement> getAllSensorMeasurements() {
		List<Measurement> result = null;

		EntityManager em = emf.createEntityManager();
		try {
			Query q = em.createNamedQuery("AllMeasurements");
			q.setMaxResults(500);
			result = q.getResultList();
		} catch (Exception e) {
		}

		em.close();
		return result;
	}

	public Measurement getLastSensorMeasurement(String sensorId) {
		Measurement result = null;

		EntityManager em = emf.createEntityManager();
		try {
			Query q = em.createNamedQuery("GetLastSensorMeasurement");
			q.setParameter("paramSensorId", sensorId);
			result = (Measurement) q.getSingleResult();
		} catch (Exception e) {
		}

		em.close();
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Sensor> getListOfSensors() throws GetSensorsException {
		List<Sensor> result = new ArrayList<Sensor>();

		EntityManager em = emf.createEntityManager();
		try {
			Query q = em.createNamedQuery("GetListOfSensors");
			result = q.getResultList();
		} catch (Exception e) {
			throw new GetSensorsException(e);
		}

		em.close();
		return result;
	}

	public Sensor getSensorById(String sensorId) throws GetSensorByIdException {
		Sensor result = null;

		EntityManager em = emf.createEntityManager();
		try {
			Query q = em.createNamedQuery("GetSensorById");
			q.setParameter("paramSensorId", sensorId);
			result = (Sensor) q.getSingleResult();
		} catch (Exception e) {
			throw new GetSensorByIdException(e);
		}

		em.close();
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Vehicle> getListOfVehicles() throws GetVehiclesException {
		List<Vehicle> result = new ArrayList<Vehicle>();

		EntityManager em = emf.createEntityManager();
		try {
			Query q = em.createNamedQuery("GetListOfVehicles");
			result = q.getResultList();
		} catch (Exception e) {
			throw new GetVehiclesException(e);
		}

		em.close();
		return result;
	}

	public Vehicle getVehicleById(String vehicleId) throws GetVehicleByIdException {
		Vehicle result = null;

		EntityManager em = emf.createEntityManager();
		try {
			Query q = em.createNamedQuery("GetVehicleById");
			q.setParameter("paramVehicleId", vehicleId);
			result = (Vehicle) q.getSingleResult();
		} catch (Exception e) {
			throw new GetVehicleByIdException(e);
		}

		em.close();
		return result;
	}

	public Double getAirMass(String vehicleId) throws GetVehicleByIdException {
		Vehicle vehicle = this.getVehicleById(vehicleId);
		Double airMass = vehicle.getAirMass();
		return airMass;
	}

	public int setAirMass(String vehicleId, Double airMass) throws SetAirMassException {
		int result = 0;

		if (vehicleId == null) {
			return result;
		}

		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			Query q = em.createNamedQuery("SetAirMass");
			q.setParameter("paramAirMass", airMass);
			q.setParameter("paramVehicleId", vehicleId);
			result = q.executeUpdate();
			em.getTransaction().commit();
		} catch (Exception e) {
			throw new SetAirMassException(e);
		}
		em.close();

		return result;
	}

	public Double getFuelMass(String vehicleId) throws GetVehicleByIdException {
		Vehicle vehicle = this.getVehicleById(vehicleId);
		Double fuelMass = vehicle.getFuelMass();
		return fuelMass;
	}

	public int setFuelMass(String vehicleId, Double fuelMass) throws SetFuelMassException {
		int result = 0;

		if (vehicleId == null) {
			return result;
		}

		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			Query q = em.createNamedQuery("SetFuelMass");
			q.setParameter("paramFuelMass", fuelMass);
			q.setParameter("paramVehicleId", vehicleId);
			result = q.executeUpdate();
			em.getTransaction().commit();
		} catch (Exception e) {
			throw new SetFuelMassException(e);
		}
		em.close();

		return result;
	}

	public Double getCO2EmissionsMass(String vehicleId) throws GetVehicleByIdException {
		Vehicle vehicle = this.getVehicleById(vehicleId);
		Double co2EmissionsMass = vehicle.getCO2EmissionsMass();
		return co2EmissionsMass;
	}

	public int setCO2EmissionsMass(String vehicleId, Double co2EmissionsMass) throws SetCO2EmissionsMassException {
		int result = 0;

		if (vehicleId == null) {
			return result;
		}

		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			Query q = em.createNamedQuery("SetCO2EmissionsMass");
			q.setParameter("paramCO2EmissionsMass", co2EmissionsMass);
			q.setParameter("paramVehicleId", vehicleId);
			result = q.executeUpdate();
			em.getTransaction().commit();
		} catch (Exception e) {
			throw new SetCO2EmissionsMassException(e);
		}
		em.close();

		return result;
	}

	public boolean isEcoState(String vehicleId) throws GetVehicleByIdException {
		Vehicle vehicle = this.getVehicleById(vehicleId);
		boolean isEcoState = vehicle.isEcoState();
		return isEcoState;
	}

	public int setVehicleEcoState(String vehicleId, boolean isEcoState) throws SetVehicleEcoStateException {
		int result = 0;

		if (vehicleId == null) {
			return result;
		}

		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			Query q = em.createNamedQuery("SetVehicleEcoState");
			q.setParameter("paramIsEcoState", isEcoState);
			q.setParameter("paramVehicleId", vehicleId);
			result = q.executeUpdate();
			em.getTransaction().commit();
		} catch (Exception e) {
			throw new SetVehicleEcoStateException(e);
		}
		em.close();

		return result;
	}

	public boolean isAutoRegulationActuatorState(String vehicleId) throws GetVehicleByIdException {
		Vehicle vehicle = this.getVehicleById(vehicleId);
		boolean isAutoRegulationActuatorState = vehicle.isAutoRegulationActuatorState();
		return isAutoRegulationActuatorState;
	}

	public int setVehicleAutoRegulationActuatorState(String vehicleId, boolean isAutoRegulationActuatorState) throws SetVehicleAutoRegulationActuatorStateException {
		int result = 0;

		if (vehicleId == null) {
			return result;
		}

		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			Query q = em.createNamedQuery("SetVehicleAutoRegulationActuatorState");
			q.setParameter("paramIsAutoRegulationActuatorState", isAutoRegulationActuatorState);
			q.setParameter("paramVehicleId", vehicleId);
			result = q.executeUpdate();
			em.getTransaction().commit();
		} catch (Exception e) {
			throw new SetVehicleAutoRegulationActuatorStateException(e);
		}
		em.close();

		return result;
	}

	public int getEmissionsLimit(String vehicleId) throws GetVehicleByIdException {
		Vehicle vehicle = this.getVehicleById(vehicleId);
		int co2EmissionsLimit = vehicle.getCo2EmissionsLimit();
		return co2EmissionsLimit;
	}

	public int setCO2EmissionsLimit(String vehicleId, int co2EmissionsLimit) throws SetCO2EmissionsLimitException {
		int result = 0;

		if (vehicleId == null) {
			return result;
		}

		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			Query q = em.createNamedQuery("SetCO2EmissionsLimit");
			q.setParameter("paramCO2EmissionsLimit", co2EmissionsLimit);
			q.setParameter("paramVehicleId", vehicleId);
			result = q.executeUpdate();
			em.getTransaction().commit();
		} catch (Exception e) {
			throw new SetCO2EmissionsLimitException(e);
		}
		em.close();

		return result;
	}
}
