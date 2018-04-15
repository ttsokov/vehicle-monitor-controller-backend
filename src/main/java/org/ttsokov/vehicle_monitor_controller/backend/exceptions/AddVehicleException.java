package org.ttsokov.vehicle_monitor_controller.backend.exceptions;

public class AddVehicleException extends Exception {
	private Exception exception;

	public AddVehicleException(Exception e) {
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
