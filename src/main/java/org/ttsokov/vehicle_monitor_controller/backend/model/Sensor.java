package org.ttsokov.vehicle_monitor_controller.backend.model;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

@Entity
@NamedQueries({
		@NamedQuery(name = "GetSensorById", query = "select s from Sensor s where s.id = :paramSensorId"),
		@NamedQuery(name = "GetListOfSensors", query = "select s from Sensor s"),
		@NamedQuery(name = "GetSensorsByVehicleId", query = "select s from Sensor s where s.vehicleId = :paramVehicleId"), })
public class Sensor implements Serializable {

	private static final long serialVersionUID = 1L;

	public Sensor() {
	}

	@Id
	private String id;

	private String type;

	private String description;

	private String vehicleId;

	private Measurement lastMeasurement;

	@OneToMany
	private Collection<Measurement> measurements;

	public Measurement getLastMeasurement() {
		return lastMeasurement;
	}

	public void setLastMeasurement(Measurement lastMeasurement) {
		this.lastMeasurement = lastMeasurement;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String param) {
		this.type = param;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String param) {
		this.description = param;
	}

	public Collection<Measurement> getMeasurements() {
		return measurements;
	}

	public void setMeasurements(Collection<Measurement> param) {
		this.measurements = param;
	}

	public String getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(String vehicleId) {
		this.vehicleId = vehicleId;
	}
}
