package org.ttsokov.vehicle_monitor_controller.backend.exceptions;

public class DeleteMeasurementException extends Exception {
	private Exception exception;

	public DeleteMeasurementException(Exception e) {
		this.exception = e;
	}

	@Override
	public String getMessage() {
		String msg = "Error while deleting measurement: ";

		if (this.exception != null) {
			msg += this.exception.getMessage();
		}

		return msg;
	}
}
