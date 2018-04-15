package org.ttsokov.vehicle_monitor_controller.backend.model;

import static javax.persistence.GenerationType.AUTO;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

// Sensor have type, i.e. MAP, AFR
// Measurement have value unit

@Entity
@NamedQueries({

		@NamedQuery(name = "AllMeasurements", query = "select m from Measurement m"),

		@NamedQuery(name = "GetMeasurementById", query = "select m from Measurement m where m.id = :paramMeasurementId"),

		@NamedQuery(name = "GetAllMeasurementsBySensorId", query = "select m from Measurement m where m.sensorId = :paramSensorId"),

		@NamedQuery(name = "GetLastSensorMeasurement", query = "select m from Measurement m where m.sensorId = :paramSensorId and m.storedAt =  (SELECT MAX(r.storedAt) from Measurement r where r.sensorId = :paramSensorId)"),

		@NamedQuery(name = "GetLastSensorMeasurements", query = "select p from Measurement p where p.sensorId = :paramSensorId order by p.storedAt DESC") })
public class Measurement implements Serializable {

	private static final long serialVersionUID = 1L;

	public Measurement() {
	}

	@Id
	@GeneratedValue(strategy = AUTO)
	private Long id;

	private String unit;

	private Timestamp storedAt;

	private Double value;

	private String sensorId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Timestamp getStoredAt() {
		return storedAt;
	}

	public void setStoredAt(Timestamp dateStored) {
		this.storedAt = dateStored;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getSensorId() {
		return sensorId;
	}

	public void setSensorId(String param) {
		this.sensorId = param;
	}

}
