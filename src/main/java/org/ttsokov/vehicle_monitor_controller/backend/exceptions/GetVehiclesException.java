package org.ttsokov.vehicle_monitor_controller.backend.exceptions;

public class GetVehiclesException extends Exception {
	private Exception exception;

	public GetVehiclesException(Exception e) {
		this.exception = e;
	}

	@Override
	public String getMessage() {
		String msg = "Error while get vehicles: ";

		if (this.exception != null) {
			msg += this.exception.getMessage();
		}

		return msg;
	}
}
