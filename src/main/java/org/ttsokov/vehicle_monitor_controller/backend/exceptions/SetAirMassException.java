package org.ttsokov.vehicle_monitor_controller.backend.exceptions;

public class SetAirMassException extends Exception {
	private Exception exception;

	public SetAirMassException(Exception e) {
		this.exception = e;
	}

	@Override
	public String getMessage() {
		String msg = "Error while set vehicle air mass: ";

		if (this.exception != null) {
			msg += this.exception.getMessage();
		}

		return msg;
	}
}
