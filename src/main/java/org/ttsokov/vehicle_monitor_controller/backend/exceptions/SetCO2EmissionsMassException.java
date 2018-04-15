package org.ttsokov.vehicle_monitor_controller.backend.exceptions;

public class SetCO2EmissionsMassException extends Exception {
	private Exception exception;

	public SetCO2EmissionsMassException(Exception e) {
		this.exception = e;
	}

	@Override
	public String getMessage() {
		String msg = "Error while set vehicle CO2 emissions mass: ";

		if (this.exception != null) {
			msg += this.exception.getMessage();
		}

		return msg;
	}
}
