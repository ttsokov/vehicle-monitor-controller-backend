package org.ttsokov.vehicle_monitor_controller.backend.exceptions;

public class ExtractMeasurementException extends Exception {
	private Exception exception;

	public ExtractMeasurementException(Exception e) {
		this.exception = e;
	}

	public ExtractMeasurementException() {
	}

	@Override
	public String getMessage() {
		String msg = "Can't extract measurement: ";

		if (this.exception != null) {
			msg += this.exception.getMessage();
		} else {
			msg += "Sensor id is not defined";
		}

		return msg;
	}
}
