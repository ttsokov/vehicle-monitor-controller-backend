package org.ttsokov.vehicle_monitor_controller.backend.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

public class NullMeasurement extends Measurement implements Serializable {
	public Long getId() {
		return (long) 1;
	}

	public String getUnit() {
		return "Null";
	}

	public Timestamp getStoredAt() {
		Date date = new Date();
		return new Timestamp(date.getTime());
	}

	public Double getValue() {
		return 1.0;
	}

	public String getSensorId() {
		return "Null";
	}
}
