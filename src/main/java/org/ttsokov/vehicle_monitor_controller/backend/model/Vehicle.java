package org.ttsokov.vehicle_monitor_controller.backend.model;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

@Entity
@NamedQueries({ @NamedQuery(name = "GetListOfVehicles", query = "select v from Vehicle v"), @NamedQuery(name = "GetVehicleById", query = "select v from Vehicle v where v.id = :paramVehicleId"),
		@NamedQuery(name = "SetVehicleEcoState", query = "update Vehicle v set v.isEcoState = :paramIsEcoState where v.id = :paramVehicleId"),
		@NamedQuery(name = "SetVehicleAutoRegulationActuatorState", query = "update Vehicle v set v.isAutoRegulationActuatorState = :paramIsAutoRegulationActuatorState where v.id = :paramVehicleId"),
		@NamedQuery(name = "SetAirMass", query = "update Vehicle v set v.airMass = :paramAirMass where v.id = :paramVehicleId"),
		@NamedQuery(name = "SetFuelMass", query = "update Vehicle v set v.fuelMass = :paramFuelMass where v.id = :paramVehicleId"),
		@NamedQuery(name = "SetCO2EmissionsMass", query = "update Vehicle v set v.co2EmissionsMass = :paramCO2EmissionsMass where v.id = :paramVehicleId"),
		@NamedQuery(name = "SetCO2EmissionsLimit", query = "update Vehicle v set v.co2EmissionsLimit = :paramCO2EmissionsLimit where v.id = :paramVehicleId"), })
public class Vehicle implements Serializable {
	private static final long serialVersionUID = 1L;

	public Vehicle() {
	}

	@Id
	private String id;

	private String model;
	private Double engineCapacity;// cm3
	private Double airMass = 0.00;// mg/cycle
	private Double fuelMass = 0.00;// mg/cycle
	private Double co2EmissionsMass = 0.00;// mg/cycle
	private boolean isEcoState;
	private boolean isAutoRegulationActuatorState;
	private int co2EmissionsLimit = 0;

	@OneToMany
	private Collection<Sensor> sensors;

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Double getEngineCapacity() {
		return engineCapacity;
	}

	public void setEngineCapacity(Double engine) {
		this.engineCapacity = engine;
	}

	public Double getAirMass() {
		return airMass;
	}

	public void setAirMass(Double airMass) {
		this.airMass = airMass;
	}

	public Double getFuelMass() {
		return fuelMass;
	}

	public void setFuelMass(Double fuelMass) {
		this.fuelMass = fuelMass;
	}

	public Double getCO2EmissionsMass() {
		return co2EmissionsMass;
	}

	public void setCO2EmissionsMass(Double co2EmissionsMass) {
		this.co2EmissionsMass = co2EmissionsMass;
	}

	public boolean isEcoState() {
		return isEcoState;
	}

	public void setEcoState(boolean isEcoState) {
		this.isEcoState = isEcoState;
	}

	public int getCo2EmissionsLimit() {
		return co2EmissionsLimit;
	}

	public void setCo2EmissionsLimit(int co2EmissionsLimit) {
		this.co2EmissionsLimit = co2EmissionsLimit;
	}

	public Collection<Sensor> getSensors() {
		return sensors;
	}

	public void setSensors(Collection<Sensor> sensors) {
		this.sensors = sensors;
	}

	public boolean isAutoRegulationActuatorState() {
		return isAutoRegulationActuatorState;
	}

	public void setAutoRegulationActuatorState(boolean isAutoRegulationActuatorState) {
		this.isAutoRegulationActuatorState = isAutoRegulationActuatorState;
	}
}
