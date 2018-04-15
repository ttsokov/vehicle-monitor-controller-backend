package org.ttsokov.vehicle_monitor_controller.backend.exceptions;

public class GetSensorsException extends Exception {

	private static final long serialVersionUID = 1L;
	private Exception exception;

	public GetSensorsException(Exception e) {
		exception = e;
	}

	@Override
	public String getMessage() {
		String msg = "Error while getting sensors: ";

		if (this.exception != null) {
			msg += this.exception.getMessage();
		}

		return msg;
	}
}
