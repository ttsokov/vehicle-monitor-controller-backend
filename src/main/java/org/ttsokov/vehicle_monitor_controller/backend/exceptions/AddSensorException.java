package org.ttsokov.vehicle_monitor_controller.backend.exceptions;

public class AddSensorException extends Exception {
	private static final long serialVersionUID = 1L;
	private Exception exception;

	public AddSensorException(Exception e) {
		this.exception = e;
	}

	@Override
	public String getMessage() {
		String msg = "Error while add sensor: ";

		if (this.exception != null) {
			msg += this.exception.getMessage();
		}

		return msg;
	}
}
