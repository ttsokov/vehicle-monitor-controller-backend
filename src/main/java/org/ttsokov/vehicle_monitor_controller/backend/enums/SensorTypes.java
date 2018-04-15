package org.ttsokov.vehicle_monitor_controller.backend.enums;

public enum SensorTypes {
	MAP("MAP", "Bar"), AFR("AFR", "Pts"), TEMP("TEMP", "Celsius");

	private final String type;
	private final String unit;

	public String getType() {
		return type;
	}

	public String getUnit() {
		return unit;
	}

	private SensorTypes(String type, String unit) {
		this.type = type;
		this.unit = unit;
	}
}
