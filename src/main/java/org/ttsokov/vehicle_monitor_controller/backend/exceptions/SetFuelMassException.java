package org.ttsokov.vehicle_monitor_controller.backend.exceptions;

public class SetFuelMassException extends Exception {
	private Exception exception;

	public SetFuelMassException(Exception e) {
		this.exception = e;
	}

	@Override
	public String getMessage() {
		String msg = "Error while set vehicle fuel mass: ";

		if (this.exception != null) {
			msg += this.exception.getMessage();
		}

		return msg;
	}
}
