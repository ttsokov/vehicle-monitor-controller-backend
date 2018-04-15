package org.ttsokov.vehicle_monitor_controller.backend.exceptions;

public class AddMeasurementException extends Exception {
	private static final long serialVersionUID = 1L;
	private Exception exception;

	public AddMeasurementException(Exception e) {
		this.exception = e;
	}

	@Override
	public String getMessage() {
		String msg = "Error while add sensor measurement: ";

		if (this.exception != null) {
			msg += this.exception.getMessage();
		}

		return msg;
	}
}
