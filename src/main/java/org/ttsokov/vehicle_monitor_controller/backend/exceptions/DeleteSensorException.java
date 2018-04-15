package org.ttsokov.vehicle_monitor_controller.backend.exceptions;

public class DeleteSensorException extends Exception {
	private Exception exception;

	public DeleteSensorException(Exception e) {
		this.exception = e;
	}

	@Override
	public String getMessage() {
		String msg = "Error while deleting sensor: ";

		if (this.exception != null) {
			msg += this.exception.getMessage();
		}

		return msg;
	}
}
