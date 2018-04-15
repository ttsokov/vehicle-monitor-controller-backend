package org.ttsokov.vehicle_monitor_controller.backend.exceptions;

public class ExtractSensorException extends Exception {
	private Exception exception;

	public ExtractSensorException(Exception e) {
		this.exception = e;
	}

	public ExtractSensorException() {
	}

	@Override
	public String getMessage() {
		String msg = "Can't extract sensor: ";

		if (this.exception != null) {
			msg += this.exception.getMessage();
		} else {
			msg += "Sensor id is not defined";
		}

		return msg;
	}
}
