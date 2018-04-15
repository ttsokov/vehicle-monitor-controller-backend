package org.ttsokov.vehicle_monitor_controller.backend.exceptions;

public class SetVehicleEcoStateException extends Exception {
	private Exception exception;

	public SetVehicleEcoStateException(Exception e) {
		this.exception = e;
	}

	@Override
	public String getMessage() {
		String msg = "Error while set vehicle isEcoState: ";

		if (this.exception != null) {
			msg += this.exception.getMessage();
		}

		return msg;
	}
}
